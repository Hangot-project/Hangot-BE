package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;
import com.hanyang.datacrawler.exception.CrawlStopException;
import com.hanyang.datacrawler.exception.NoCrawlNextDayException;
import com.hanyang.datacrawler.infrastructure.RabbitMQPublisher;
import com.hanyang.datacrawler.service.DataCrawler;
import com.hanyang.datacrawler.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataGoKrCrawler implements DataCrawler {

    private final RestTemplate restTemplate;
    private final DataGoKrHtmlParser htmlParser;
    private final DataGoKrFileDownloadService dataGoKrFileDownloadService;
    private final DownloadParameterExtractor downloadParameterExtractor;
    private final RabbitMQPublisher rabbitMQPublisher;

    private static final String DATASET_LIST_URL = "https://www.data.go.kr/tcs/dss/selectDataSetList.do?dType=FILE";
    private final DatasetService datasetService;

    @Override
    public String getSiteName() {
        return "data.go.kr";
    }

    @Override
    public List<Dataset> crawlDatasetsPage(int pageNo, int pageSize) {
        String url = buildPageUrl(pageNo, pageSize);
        String html = restTemplate.getForObject(url, String.class);

        List<String> datasetUrls = htmlParser.parseDatasetUrls(html);

        List<Dataset> result = new ArrayList<>();

        for (String datasetUrl : datasetUrls) {
            try {
                crawlSingleDataset(datasetUrl).ifPresentOrElse(
                        dataset -> {
                            result.add(dataset);
                            downloadFile(dataset);
                        },
                        () -> log.warn("크롤링 실패 - {}", datasetUrl)
                );
            } catch (CrawlStopException e) {
                throw e;
            }
        }
        return result;
    }

    @Override
    public Optional<Dataset> crawlSingleDataset(String datasetUrl) {

        try {
            String html = restTemplate.getForObject(datasetUrl, String.class);
            
            DatasetWithTag dataset = htmlParser.parseDatasetDetailPage(html, datasetUrl);
            
            Dataset savedDataset =  datasetService.saveDatasetWithTag(dataset.getDataset(),dataset.getTags());
            return Optional.of(savedDataset);

        } catch (NoCrawlNextDayException e) {
            return Optional.empty();
        } catch (Exception throwable) {
            log.error("단일 데이터셋 크롤링 실패: {}", throwable.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void downloadFile(Dataset dataset) {
        String sourceUrl = dataset.getSourceUrl();
        downloadParameterExtractor.extractDownloadParams(sourceUrl).ifPresent(params -> processDownload(dataset, params));
    }
    
    private void processDownload(Dataset dataset, Object params) {
        try {
            FileDownloadParams downloadParams = (FileDownloadParams) params;
            String folderName = String.valueOf(dataset.getDatasetId());
            String fileName = dataset.getResourceName() + "." + dataset.getType();
            
            String downloadUrl = dataGoKrFileDownloadService.buildDataGoDownloadUrl(
                    downloadParams.atchFileId(), downloadParams.fileDetailSn(), fileName);
            
            String resourceUrl = dataGoKrFileDownloadService.downloadAndUploadFile(downloadUrl, folderName, fileName);
            
            if (resourceUrl != null) {
                datasetService.updateResourceUrl(dataset,resourceUrl);
                log.debug("파일 다운로드 성공: {}", dataset.getTitle());
                rabbitMQPublisher.sendMessage(dataset.getDatasetId().toString());
            } else {
                log.warn("파일 다운로드 실패 - 데이터셋 ID: {}", dataset.getDatasetId());
            }
        } catch (Exception e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    private String buildPageUrl(int pageNo, int pageSize) {
        return DATASET_LIST_URL +
                "&currentPage=" + pageNo +
                "&perPage=" + pageSize;
    }
}