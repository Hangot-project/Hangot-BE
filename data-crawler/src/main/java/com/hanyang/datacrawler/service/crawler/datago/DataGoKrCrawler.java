package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.dto.MessageDto;
import com.hanyang.datacrawler.exception.CrawlStopException;
import com.hanyang.datacrawler.exception.NoCrawlNextDayException;
import com.hanyang.datacrawler.infrastructure.RabbitMQPublisher;
import com.hanyang.datacrawler.service.DataCrawler;
import com.hanyang.datacrawler.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataGoKrCrawler implements DataCrawler {

    private final RestTemplate restTemplate;
    private final DataGoKrHtmlParser htmlParser;
    private final DataGoKrFileDownloadService fileDownloadService;
    private final DataGoKrDownloadParamExtractor downloadParamExtractor;
    private final RabbitMQPublisher rabbitMQPublisher;

    private static final String DATASET_LIST_URL = "https://www.data.go.kr/tcs/dss/selectDataSetList.do?dType=FILE";
    private static final Set<String> SUPPORTED_FILE_TYPES = Set.of(
            "csv", "xlsx", "xls", "xlsm", "xlsb", "xltx", "xltm");
    private final DatasetService datasetService;

    @Override
    public String getSiteName() {
        return "data.go.kr";
    }

    @Override
    public void crawlDatasetsPage(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate) {
        String url = buildPageUrl(pageNo, pageSize);
        String html = restTemplate.getForObject(url, String.class);

        // 페이지의 마지막 데이터셋 날짜 확인하여 스킵 여부 결정
        if (shouldSkipPage(html, endDate)) {
            log.info("페이지 {} 스킵: 날짜 범위 밖 데이터만 존재", pageNo);
            return;
        }

        List<String> datasetUrls = htmlParser.parseDatasetUrls(html);
        
        List<DatasetWithTag> datasets = new ArrayList<>();
        
        for (String datasetUrl : datasetUrls) {
            try {
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
                extractResourceURL(dataset);
            }
            log.info("데이터셋 배치 저장 완료 - 저장된 데이터셋 개수: {}", savedDatasets.size());
        }
    }

    @Override
    public Optional<DatasetWithTag> crawlSingleDataset(String datasetUrl, LocalDate startDate, LocalDate endDate) {
        log.info("단일 데이터셋 크롤링 시작 - URL: {}", datasetUrl);
        String html = restTemplate.getForObject(datasetUrl, String.class);
        DatasetWithTag dataset = htmlParser.parseMetaData(html, datasetUrl, startDate, endDate);
        return Optional.of(dataset);
    }

    @Override
    public void extractResourceURL(Dataset dataset) {
        String sourceUrl = dataset.getSourceUrl();
        downloadParamExtractor.extractDownloadParams(sourceUrl).ifPresent(params -> handleFileDownload(dataset, params));
    }

    private boolean shouldSkipPage(String html, LocalDate endDate) {
        if (endDate == null) return false;
        
        Optional<LocalDate> lastDateOpt = htmlParser.getLastDatasetDateFromPage(html);

        if (lastDateOpt.isEmpty()) return false;

        LocalDate lastDate = lastDateOpt.get();
        return lastDate.isAfter(endDate);
    }

    private void handleFileDownload(Dataset dataset, Object params) {
        try {
            FileDownloadParams downloadParams = (FileDownloadParams) params;
            String downloadUrl = buildDownloadUrl(downloadParams, dataset.getResourceName());
            
            if (isSupportedFileType(dataset.getType())) {
                downloadFileToS3(dataset, downloadUrl);
            } else {
                saveDownloadUrlOnly(dataset, downloadUrl);
            }
        } catch (Exception e) {
            log.error("파일 다운로드 처리 중 오류 발생: {}", e.getMessage());
        }
    }
    
    private String buildDownloadUrl(FileDownloadParams downloadParams, String fileName) {
        return fileDownloadService.buildDataGoDownloadUrl(
                downloadParams.atchFileId(), downloadParams.fileDetailSn(), fileName);
    }
    
    private void saveDownloadUrlOnly(Dataset dataset, String downloadUrl) {
        log.info("지원하지 않는 파일 형식({}): {} - S3 업로드 스킵", dataset.getType(), dataset.getTitle());
        datasetService.updateResourceUrl(dataset, downloadUrl);
    }
    
    private void downloadFileToS3(Dataset dataset, String downloadUrl) {
        String folderName = String.valueOf(dataset.getDatasetId());
        String fileName = dataset.getResourceName();
        
        String s3Url = fileDownloadService.downloadAndUploadFile(
                downloadUrl, folderName, fileName, dataset.getSourceUrl());
        
        if (s3Url != null) {
            Dataset updatedDataset = datasetService.updateResourceUrl(dataset, downloadUrl);
            rabbitMQPublisher.sendMessage(MessageDto.builder().
                    datasetId(String.valueOf(updatedDataset.getDatasetId())).
                    resourceUrl(updatedDataset.getResourceUrl()).
                    sourceUrl(updatedDataset.getSourceUrl()).
                    build());
            log.info("메세지 큐 요청: 데이터셋 제목 - {}", updatedDataset.getResourceUrl());
        }
    }
    
    private boolean isSupportedFileType(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            return false;
        }
        return SUPPORTED_FILE_TYPES.contains(fileType.toLowerCase().trim());
    }

    private String buildPageUrl(int pageNo, int pageSize) {
        return DATASET_LIST_URL +
                "&currentPage=" + pageNo +
                "&perPage=" + pageSize;
    }
}