package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.config.CrawlerConfig;
import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithThemeDto;
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
    public int getMaxPages() {
        return CrawlerConfig.MAX_PAGES;
    }

    @Override
    public List<Dataset> crawlDatasetsPage(int pageNo, int pageSize) {
        String url = buildPageUrl(pageNo, pageSize);
        String html = restTemplate.getForObject(url, String.class);
        log.debug("{}페이지 HTML 파싱 시작", pageNo);

        List<String> datasetUrls = htmlParser.parseDatasetUrls(html);
        log.debug("{}페이지에서 {}개 데이터셋 URL 추출", pageNo, datasetUrls.size());

        List<Dataset> result = new ArrayList<>();

        for (String datasetUrl : datasetUrls) {
            crawlSingleDataset(datasetUrl).ifPresentOrElse(
                    dataset -> {
                        result.add(dataset);
                        downloadFile(dataset);
                    },
                    () -> log.warn("크롤링 실패 - {}", datasetUrl)
            );
        }
        log.debug("{}개 데이터셋 크롤링 완료", result.size());
        return result;
    }

    @Override
    public Optional<Dataset> crawlSingleDataset(String datasetUrl) {
        log.info("단일 데이터셋 크롤링 시작: {}", datasetUrl);

        try {
            String html = restTemplate.getForObject(datasetUrl, String.class);
            
            DatasetWithThemeDto dataset = htmlParser.parseDatasetDetailPage(html, datasetUrl);
            Dataset savedDataset =  datasetService.saveDatasetWithTheme(dataset.getDataset(),dataset.getThemes());
            log.info("단일 데이터셋 크롤링 완료: {}", dataset.getDataset().getTitle());
            return Optional.of(savedDataset);

        } catch (Exception throwable) {
            log.error("단일 데이터셋 크롤링 실패: {}", throwable.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void downloadFile(Dataset dataset) {
        String sourceUrl = dataset.getSourceUrl();
        log.info("파일 다운로드 시작 - 데이터셋 ID: {}", dataset.getDatasetId());
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
                log.info("파일 다운로드 성공 - 데이터셋 ID: {}, URL: {}", dataset.getDatasetId(), resourceUrl);
                rabbitMQPublisher.sendMessage(dataset.getDatasetId().toString());
                log.info("메세지 전송 - 데이터셋 ID: {}", dataset.getDatasetId());
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