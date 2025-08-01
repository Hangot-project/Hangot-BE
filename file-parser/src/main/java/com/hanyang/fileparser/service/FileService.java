package com.hanyang.fileparser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

                    // ZIP 파일 매직 넘버 체크 (XLSX 파일은 제외)
                    if (isUnsupportedZipFile(tempFile)) {
                        Files.deleteIfExists(tempFile);
                        throw new IllegalArgumentException("ZIP 파일은 지원하지 않습니다");
                    }

                    return tempFile;
                }
        );
    }

    private boolean isUnsupportedZipFile(Path file) {
        try {
            // 첫 2바이트로 ZIP 파일인지 확인
            byte[] header = new byte[2];
            try (InputStream headerStream = Files.newInputStream(file)) {
                int bytesRead = headerStream.read(header);
                if (bytesRead < 2 || header[0] != 0x50 || header[1] != 0x4B) {
                    return false; // ZIP 파일이 아님
                }
            }

            // ZIP 파일이면 XLSX/DOCX 등 Office 문서인지 확인
            try (ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(file))) {
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    // Office 문서의 특징적인 파일들 확인
                    if (entryName.equals("[Content_Types].xml") || 
                        entryName.equals("_rels/.rels") ||
                        entryName.startsWith("xl/") || // Excel
                        entryName.startsWith("word/") || // Word
                        entryName.startsWith("ppt/")) { // PowerPoint
                        return false;
                    }
                }
            }
            
            return true; // 일반 ZIP 파일이므로 차단
        } catch (IOException e) {
            return false; // 읽기 실패시 허용 (기존 동작 유지)
        }
    }
}