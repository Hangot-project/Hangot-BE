package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.dto.MessageDto;
import com.hanyang.datacrawler.exception.SkipPageException;
import com.hanyang.datacrawler.infrastructure.RabbitMQPublisher;
import com.hanyang.datacrawler.service.DataCrawler;
import com.hanyang.datacrawler.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataGoKrCrawler implements DataCrawler {

    private final RestTemplate restTemplate;
    private final DataGoKrHtmlParser htmlParser;
    private final DataGoKrDownloadParamExtractor downloadParamExtractor;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final DatasetService datasetService;


    @Value("${crawler.delay.request:2000}")
    private int requestDelay;
    
    @Value("${crawler.delay.page:5000}")
    private int pageDelay;

    private static final String DATA_GO_KR_BASE_URL = "https://www.data.go.kr";
    private static final String DATASET_LIST_URL = "https://www.data.go.kr/tcs/dss/selectDataSetList.do?dType=FILE";

    @Override
    public String getSiteDomain() {
        return DATA_GO_KR_BASE_URL;
    }

    @Override
    public List<String> getSourceUrlList(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate) {
        String url = buildPageUrl(pageNo, pageSize);
        String html = getHtmlWithHeaders(url);

        List<String> datasetUrls = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements datasetElements = doc.select("#fileDataList li");

        Element lastDataset = datasetElements.get(datasetElements.size() - 1);

        // 페이지의 마지막 데이터셋 날짜 확인하여 스킵 여부 결정
        htmlParser.getDatasetDate(lastDataset).ifPresent((updateDate)->{
            if(updateDate.isAfter(endDate)) throw new SkipPageException(pageNo) ;
        });

        for (Element element : datasetElements) {
            Optional<String> optionalUrl = parseDatasetUrl(element);
            Optional<LocalDate> optionalDate = htmlParser.getDatasetDate(element);

            if (optionalUrl.isEmpty()) continue;

            if (optionalDate.isPresent()) {
                LocalDate updateDate = optionalDate.get();

                if (updateDate.isAfter(endDate)) continue;

                if (updateDate.isBefore(startDate)) break;
            }

            datasetUrls.add(optionalUrl.get());
        }

        return datasetUrls;
    }

    public void crawlDataset(List<String> sourceUrls){

        List<DatasetWithTag> datasets = new ArrayList<>();

        for (String datasetUrl :sourceUrls) {
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
                String resourceURL = downloadParamExtractor.extractDownloadParams(dataset.getSourceUrl())
                        .map(params -> {
                            String fileName = FilenameUtils.removeExtension(dataset.getResourceName());
                            return buildDownloadUrl(params, fileName);
                        })
                        .orElse("");

                Dataset updatedDataset = datasetService.updateResourceUrl(dataset, resourceURL);
                sendDataParsingRequest(updatedDataset);
            }
            log.info("데이터셋 배치 저장 완료 - 저장된 데이터셋 개수: {}", savedDatasets.size());
        }
        
        try {
            Thread.sleep(pageDelay);
        } catch (InterruptedException e) {
            //무시
        }
    }

    @Override
    public Optional<DatasetWithTag> crawlSingleDataset(String datasetUrl) {
        log.info("단일 데이터셋 크롤링 시작 - URL: {}", datasetUrl);
        String html = getHtmlWithHeaders(datasetUrl);
        DatasetWithTag dataset = htmlParser.parseMetaData(html, datasetUrl);
        return Optional.of(dataset);
    }

    private String buildDownloadUrl(FileDownloadParams downloadParams, String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return "https://www.data.go.kr/cmm/cmm/fileDownload.do"
                + "?atchFileId=" + downloadParams.atchFileId()
                + "&fileDetailSn=" + downloadParams.fileDetailSn()
                + "&dataNm=" + encoded;
    }

    private void sendDataParsingRequest(Dataset dataset) {
        rabbitMQPublisher.sendMessage(MessageDto.builder().
                datasetId(String.valueOf(dataset.getDatasetId())).
                sourceUrl(dataset.getSourceUrl()).
                resourceUrl(dataset.getResourceUrl()).
                type(dataset.getType()).
                build());
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
    
    private String buildPageUrl(int pageNo, int pageSize) {
        return DATASET_LIST_URL +
                "&currentPage=" + pageNo +
                "&perPage=" + pageSize;
    }

    private Optional<String> parseDatasetUrl(Element element) {
        try {
            Element linkElement = element.selectFirst("a[href*='/data/']");
            if (linkElement == null) return Optional.empty();
            String datasetUrl = linkElement.attr("href");
            if (datasetUrl.isEmpty()) return Optional.empty();
            String resourceUrl = datasetUrl.startsWith("/") ? DATA_GO_KR_BASE_URL + datasetUrl : datasetUrl;
            return Optional.of(resourceUrl);
        } catch (Exception e) {
            log.error("데이터셋 요소 파싱 중 오류: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}