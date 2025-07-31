package com.hanyang.fileparser.service;

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
                    Path tempFile = Files.createTempFile("download-", "." + type);
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                    // ZIP 파일 매직 넘버 체크 (첫 2바이트만 읽어서 메모리 효율적으로)
                    byte[] header = new byte[2];
                    try (InputStream headerStream = Files.newInputStream(tempFile)) {
                        int bytesRead = headerStream.read(header);
                        if (bytesRead >= 2 && header[0] == 0x50 && header[1] == 0x4B) {
                            Files.deleteIfExists(tempFile);
                            throw new IllegalArgumentException("ZIP 파일은 지원하지 않습니다");
                        }
                    }

                    return tempFile;
                }
        );
    }
}