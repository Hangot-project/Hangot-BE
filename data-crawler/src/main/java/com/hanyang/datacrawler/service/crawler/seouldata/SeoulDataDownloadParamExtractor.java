package com.hanyang.datacrawler.service.crawler.seouldata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class SeoulDataDownloadParamExtractor {
    
    public String extractCsvDownloadUrl(String sourceUrl) {
        String datasetId = extractDatasetIdFromUrl(sourceUrl);
        return "https://datafile.seoul.go.kr/bigfile/iot/sheet/csv/download.do?infId=" + datasetId + "&srvType=S&serviceKind=1&sAction=csv";
    }
    
    public String extractFileNameFromUrl(String csvUrl) {
        try {
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("Referer", "https://data.seoul.go.kr/");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            connection.connect();
            
            String contentDisposition = connection.getHeaderField("Content-Disposition");
            connection.disconnect();
            
            if (contentDisposition != null && contentDisposition.contains("filename=")) {
                String fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
                // 따옴표 제거
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
                return fileName;
            }
            
            return null;
        } catch (IOException e) {
            log.error("파일명 추출 실패 - URL: {}, 오류: {}", csvUrl, e.getMessage());
            return null;
        }
    }
    
    private String extractDatasetIdFromUrl(String sourceUrl) {
        List<String> list = List.of(sourceUrl.split("/"));
        return list.get(2);
    }
}