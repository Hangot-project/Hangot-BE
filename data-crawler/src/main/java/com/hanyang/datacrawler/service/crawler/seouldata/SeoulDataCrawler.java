package com.hanyang.datacrawler.service.crawler.seouldata;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.dto.MessageDto;
import com.hanyang.datacrawler.exception.SkipPageException;
import com.hanyang.datacrawler.infrastructure.RabbitMQPublisher;
import com.hanyang.datacrawler.service.DataCrawler;
import com.hanyang.datacrawler.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeoulDataCrawler implements DataCrawler {

    private final RestTemplate restTemplate;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final DatasetService datasetService;
    private final SeoulDataHtmlParser htmlParser;

    @Value("${crawler.delay.request:2000}")
    private int requestDelay;
    
    @Value("${crawler.delay.page:5000}")
    private int pageDelay;

    private static final String SEOUL_BASE_URL = "https://data.seoul.go.kr";
    private static final String DATASET_LIST_URL = "https://data.seoul.go.kr/dataList/datasetList.do";

    @Override
    public String getSiteDomain() {
        return SEOUL_BASE_URL;
    }

    @Override
    public List<String> getSourceUrlList(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate) {
        String html = getPageWithFormData(pageNo);

        List<String> datasetUrls = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        
        Elements datasetElements = doc.select("dl.type-b");
        
        if (!datasetElements.isEmpty()) {
            // 페이지의 마지막 데이터셋 날짜 확인하여 스킵 여부 결정
            Element lastDataset = datasetElements.get(datasetElements.size() - 1);
            getDatasetDate(lastDataset).ifPresent((updateDate) -> {
                if (updateDate.isAfter(endDate)) throw new SkipPageException(pageNo);
            });
        }
        
        for (Element element : datasetElements) {
            Element linkElement = element.selectFirst("a.goView");
            if (linkElement == null) continue;
            
            String href = linkElement.attr("data-rel");
            if (href.isEmpty()) continue;
            
            Optional<LocalDate> optionalDate = getDatasetDate(element);
            
            if (optionalDate.isPresent()) {
                LocalDate updateDate = optionalDate.get();
                
                if (updateDate.isAfter(endDate)) continue;
                
                if (updateDate.isBefore(startDate)) break;
            }
            
            String fullUrl = SEOUL_BASE_URL + "/dataList/" + href;
            datasetUrls.add(fullUrl);
        }
        
        return datasetUrls;
    }

    @Override
    public void crawlDataset(List<String> sourceUrls) {
        List<DatasetWithTag> datasets = new ArrayList<>();

        for (String datasetUrl : sourceUrls) {
            try {
                Thread.sleep(requestDelay);
                crawlSingleDataset(datasetUrl).ifPresent(datasets::add);
            } catch (InterruptedException e) {
                //무시
            }
        }
        
        if (!datasets.isEmpty()) {
            List<Dataset> datasetList = datasets.stream().map(DatasetWithTag::getDataset).toList();
            List<List<String>> tagsList = datasets.stream().map(DatasetWithTag::getTags).toList();
            
            List<Dataset> savedDatasets = datasetService.saveDatasetsBatch(datasetList, tagsList);

            for (Dataset dataset : savedDatasets) {
                sendDataParsingRequest(dataset);
            }
            log.info("서울 열린 데이터 광장데이터셋 배치 저장 완료 - 저장된 데이터셋 개수: {}", savedDatasets.size());
        }
        
        try {
            Thread.sleep(pageDelay);
        } catch (InterruptedException e) {
            //무시
        }
    }

    @Override
    public Optional<DatasetWithTag> crawlSingleDataset(String datasetUrl) {
        log.info("서울 열린 데이터 광장 데이터셋 크롤링 시작 - sourceURL: {}", datasetUrl);
        try {
            String html = getHtmlWithHeaders(datasetUrl);
            return htmlParser.parseDatasetDetail(html, datasetUrl);
        } catch (Exception e) {
            log.error("서울 열린 데이터 광장 크롤링 실패 - sourceURL: {}", datasetUrl, e);
            return Optional.empty();
        }
    }

    private void sendDataParsingRequest(Dataset dataset) {
        rabbitMQPublisher.sendMessage(MessageDto.builder()
                .datasetId(String.valueOf(dataset.getDatasetId()))
                .sourceUrl(dataset.getSourceUrl())
                .resourceUrl(dataset.getResourceUrl())
                .type(dataset.getType())
                .build());
    }

    private String getHtmlWithHeaders(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.set("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8");
        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Connection", "keep-alive");
        headers.set("Upgrade-Insecure-Requests", "1");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
    
    private String getPageWithFormData(int pageNo) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.set("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8");
            headers.set("Referer", "https://data.seoul.go.kr/dataList/datasetList.do");
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("datasetKind", "1");
            formData.add("searchFlag", "N");
            formData.add("pageIndex", String.valueOf(pageNo));
            formData.add("sortColBy", "최신수정일순");
            formData.add("menuCd", "SHEET");
            formData.add("searchValue", "");
            formData.add("resSearch", "N");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                DATASET_LIST_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("폼 데이터로 페이지 요청 실패 - 페이지: {}, 오류: {}", pageNo, e.getMessage());
            // 폴백으로 GET 요청 시도
            return getHtmlWithHeaders(DATASET_LIST_URL + "?datasetKind=1&searchFlag=N&pageIndex=" + pageNo + "&sortColBy=최신수정일순&menuCd=SHEET");
        }
    }
    
    private Optional<LocalDate> getDatasetDate(Element element) {
        try {
            // "수정일자 :" 텍스트를 포함하는 span 찾기
            Elements infoSpans = element.select(".list-statistics-info2 span");
            for (Element span : infoSpans) {
                String text = span.text().trim();
                if (text.contains("수정일자 :")) {
                    // "수정일자 : 2025-07-29" 형태에서 날짜 부분 추출
                    String dateText = text.replace("수정일자 :", "").trim();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    return Optional.of(LocalDate.parse(dateText, formatter));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("날짜 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

}