package com.hanyang.datacrawler.service.crawler.datago;

import com.hanyang.datacrawler.service.file.FileService;
import com.hanyang.datacrawler.service.file.FileType;
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

    public void downloadAndUploadFile(String downloadUrl, String folderName, String fileName) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("Accept-Encoding", "identity");

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

                Path tempFile = Files.createTempFile("download-", "."+FileType.getFileType(fileName).getExtension());
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                try {
                    // ZIP 파일 매직 바이트 체크
                    byte[] header = new byte[4];
                    try (InputStream fileStream = Files.newInputStream(tempFile)) {
                        int bytesRead = fileStream.read(header);
                        if (bytesRead >= 4 && isZipFile(header)) {
                            throw new UnsupportedOperationException();
                        }
                    }
                    fileService.processFileInChunks(folderName, tempFile);
                } finally {
                    Files.deleteIfExists(tempFile);
                }
                return null;
            }
        );

    }

    private boolean isZipFile(byte[] header) {
        // ZIP 파일 매직 바이트: PK (0x504B)
        return header.length >= 4 && 
               header[0] == 0x50 && header[1] == 0x4B && 
               (header[2] == 0x03 || header[2] == 0x05 || header[2] == 0x07) &&
               (header[3] == 0x04 || header[3] == 0x06 || header[3] == 0x08);
    }
}
