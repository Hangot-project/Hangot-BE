package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGoKrFileDownloadService {

    private final RestTemplate restTemplate;
    private final S3StorageManager s3StorageManager;

    public String downloadAndUploadFile(String downloadUrl, String folderName, String fileName) {
        log.info("파일 다운로드 시작 - {}", downloadUrl);

        ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
        byte[] fileContent = response.getBody();

        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalStateException("다운로드된 파일이 비어 있습니다");
        }

        String resourceUrl = uploadToS3(fileContent, folderName, fileName);
        log.info("업로드 완료 - {}", resourceUrl);

        return resourceUrl;
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

    private String uploadToS3(byte[] fileContent,
                              String folderName,
                              String fileName) {

        return s3StorageManager.uploadAndGetUrl(folderName, fileName, fileContent);
    }
}
