package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.dto.MessageDto;
import com.hanyang.datacrawler.exception.CrawlStopException;
import com.hanyang.datacrawler.exception.NoCrawlNextDayException;
import com.hanyang.datacrawler.infrastructure.RabbitMQPublisher;
import com.hanyang.datacrawler.service.DataCrawler;
import com.hanyang.datacrawler.service.DatasetService;
import com.hanyang.datacrawler.service.file.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
    private final DataGoKrResourceService resourceService;
    private final DataGoKrDownloadParamExtractor downloadParamExtractor;
    private final RabbitMQPublisher rabbitMQPublisher;
    private final DatasetService datasetService;

    @Value("${crawler.delay.request:2000}")
    private int requestDelay;
    
    @Value("${crawler.delay.page:5000}")
    private int pageDelay;

    private static final String DATASET_LIST_URL = "https://www.data.go.kr/tcs/dss/selectDataSetList.do?dType=FILE";

    @Override
    public String getSiteName() {
        return "data.go.kr";
    }

    @Override
    public void crawlDatasetsPage(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate) {
        String url = buildPageUrl(pageNo, pageSize);
        String html = getHtmlWithHeaders(url);

        // 페이지의 마지막 데이터셋 날짜 확인하여 스킵 여부 결정
        if (shouldSkipPage(html, endDate)) {
            log.info("페이지 {} 스킵: 날짜 범위 밖 데이터만 존재", pageNo);
            return;
        }

        List<String> datasetUrls = htmlParser.parseDatasetUrls(html);
        
        List<DatasetWithTag> datasets = new ArrayList<>();
        
        for (String datasetUrl : datasetUrls) {
            try {
                Thread.sleep(requestDelay);
                crawlSingleDataset(datasetUrl,startDate,endDate).ifPresent(datasets::add);
            } catch (CrawlStopException e) {
                log.info("페이지 크롤링 중단: 날짜 범위 밖 데이터 도달 - {}", datasetUrl);
                throw e;
            } catch (NoCrawlNextDayException e) {
                log.info("지정일 보다 최신 데이터 : 크롤링 대상 아님 - {}", datasetUrl);
            } catch (Exception e) {
                log.error("페이지 크롤링 에러 - URL-{}  - {}",datasetUrl,e.getMessage());
            }
        }
        
        if (!datasets.isEmpty()) {
            List<Dataset> datasetList = datasets.stream().map(DatasetWithTag::getDataset).toList();
            List<List<String>> tagsList = datasets.stream().map(DatasetWithTag::getTags).toList();
            
            List<Dataset> savedDatasets = datasetService.saveDatasetsBatch(datasetList, tagsList);

            for (Dataset dataset : savedDatasets) {
                downloadParamExtractor.extractDownloadParams(dataset.getSourceUrl()).ifPresent(params -> {
                    String fileName = FilenameUtils.removeExtension(dataset.getResourceName());
                    String resourceURL = buildDownloadUrl(params, fileName);
                    Dataset updatedDataset = datasetService.updateResourceUrl(dataset, resourceURL);

                    FileType fileType = FileType.getFileType(updatedDataset.getResourceName());
                    if (fileType.IsSupportVisualization()) {
                        downloadFileToS3(updatedDataset, resourceURL);
                    }
                });
            }
            log.info("데이터셋 배치 저장 완료 - 저장된 데이터셋 개수: {}", savedDatasets.size());
        }
        
        try {
            Thread.sleep(pageDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("페이지 간 대기 중 인터럽트 발생", e);
        }
    }

    @Override
    public Optional<DatasetWithTag> crawlSingleDataset(String datasetUrl, LocalDate startDate, LocalDate endDate) {
        log.info("단일 데이터셋 크롤링 시작 - URL: {}", datasetUrl);
        String html = getHtmlWithHeaders(datasetUrl);
        DatasetWithTag dataset = htmlParser.parseMetaData(html, datasetUrl, startDate, endDate);
        return Optional.of(dataset);
    }

    private boolean shouldSkipPage(String html, LocalDate endDate) {
        if (endDate == null) return false;
        
        Optional<LocalDate> lastDateOpt = htmlParser.getLastDatasetDateFromPage(html);

        if (lastDateOpt.isEmpty()) return false;

        LocalDate lastDate = lastDateOpt.get();
        return lastDate.isAfter(endDate);
    }

    private String buildDownloadUrl(FileDownloadParams downloadParams, String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return "https://www.data.go.kr/cmm/cmm/fileDownload.do"
                + "?atchFileId=" + downloadParams.atchFileId()
                + "&fileDetailSn=" + downloadParams.fileDetailSn()
                + "&dataNm=" + encoded;
    }

    private void downloadFileToS3(Dataset dataset, String downloadUrl) {
        String folderName = String.valueOf(dataset.getDatasetId());
        String resourceName = dataset.getResourceName();

        try{
            resourceService.downloadAndUploadFile(
                    downloadUrl, folderName, resourceName);
        } catch (UnsupportedOperationException zipException) {
            //zip 파일은 파싱 안함
        } catch (Exception e) {
            log.error("파일 다운로드 중 예상치 못한 오류 - datasetId: {} sourceURL: {}, 에러: {}",dataset.getDatasetId(), dataset.getSourceUrl(), e.getMessage(), e);
        }

        rabbitMQPublisher.sendMessage(MessageDto.builder().
                datasetId(String.valueOf(dataset.getDatasetId())).
                sourceUrl(dataset.getSourceUrl()).
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
}