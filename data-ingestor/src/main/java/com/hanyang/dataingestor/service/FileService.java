package com.hanyang.dataingestor.service;

import lombok.RequiredArgsConstructor;
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
public class FileService {

    private final RestTemplate restTemplate;


    public Path downloadFile(String downloadUrl, String type) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "identity");

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

                    // It's good practice to handle potential IOException when creating or copying files
                    Path tempFile = Files.createTempFile("download-", "." + type);
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    return tempFile;
                }
        );
    }
}