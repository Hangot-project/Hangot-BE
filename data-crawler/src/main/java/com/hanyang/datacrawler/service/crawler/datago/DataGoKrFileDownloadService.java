package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGoKrFileDownloadService {

    private final RestTemplate restTemplate;
    private final S3StorageManager s3StorageManager;

    public String downloadAndUploadFile(String downloadUrl, String folderName, String fileName, String sourceUrl) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "identity");
        
        try {
            return restTemplate.execute(
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

                    // 임시 파일에서 읽어서 S3 업로드
                    try (FileInputStream fileInputStream = new FileInputStream(tempFile.toFile())) {
                        return uploadToS3(fileInputStream, folderName, fileName);
                    }
                    finally {
                        Files.deleteIfExists(tempFile);
                    }

                }
            );
            
        } catch (IllegalStateException e) {
            // 의도한 예외 - 스택트레이스 없이 로깅
            log.error("파일 다운로드 실패 - downloadURL: {}, sourceURL: {}, 에러: {}", downloadUrl, sourceUrl, e.getMessage());
            throw new RuntimeException("파일 다운로드 실패: " + downloadUrl, e);
        } catch (Exception e) {
            // 예상치 못한 시스템 오류 - 스택트레이스 포함
            log.error("파일 다운로드 중 예상치 못한 오류 - fileName: {} downloadURL: {}, sourceURL: {}, 에러: {}",fileName,downloadUrl, sourceUrl, e.getMessage(), e);
            throw new RuntimeException("파일 다운로드 실패: " + downloadUrl, e);
        }
    }

    public String buildDataGoDownloadUrl(String atchFileId,
                                         String fileDetailSn,
                                         String fileName) {

        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return "https://www.data.go.kr/cmm/cmm/fileDownload.do"
                + "?atchFileId=" + atchFileId
                + "&fileDetailSn=" + fileDetailSn
                + "&dataNm=" + encoded;
    }

    private String uploadToS3(InputStream inputStream,
                              String folderName,
                              String fileName) {

        return s3StorageManager.uploadAndGetUrl(folderName, fileName, inputStream);
    }
}
