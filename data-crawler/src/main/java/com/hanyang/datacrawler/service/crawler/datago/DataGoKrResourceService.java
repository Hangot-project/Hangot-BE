package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGoKrResourceService {

    private final RestTemplate restTemplate;
    private final FileService fileService;

    public void downloadAndUploadFile(String downloadUrl, String folderName, String fileName, String sourceUrl) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "identity");

        try {
            restTemplate.execute(
                downloadUrl,
                HttpMethod.GET,
                request -> {
                    request.getHeaders().setAll(headers.toSingleValueMap());
                },
                clientHttpResponse -> {
                    if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("파일 다운로드 실패: " + clientHttpResponse.getStatusCode());
                    }

                    InputStream inputStream = clientHttpResponse.getBody();


                    // fileName / -> _로 대체
                    String safeFileName = fileName.replaceAll("[/\\\\]", "_");
                    Path tempFile = Files.createTempFile("download-", safeFileName);
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                    try {
                        fileService.processFileInChunks(folderName, tempFile);
                    } finally {
                        Files.deleteIfExists(tempFile);
                    }
                    return null;
                }
            );

        } catch (Exception e) {
            log.error("파일 다운로드 중 예상치 못한 오류 - downloadURL: {}, sourceURL: {}, 에러: {}", downloadUrl, sourceUrl, e.getMessage(), e);
        }
    }
}
