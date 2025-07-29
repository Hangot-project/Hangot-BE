package com.hanyang.datacrawler.service.crawler.seouldata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SeoulDataDownloadParamExtractor {
    
    public String extractCsvDownloadUrl(String sourceUrl) {
        String datasetId = extractDatasetIdFromUrl(sourceUrl);
        return "https://datafile.seoul.go.kr/bigfile/iot/sheet/csv/download.do?infId=" + datasetId + "&srvType=S&serviceKind=1&sAction=csv";
    }
    
    private String extractDatasetIdFromUrl(String sourceUrl) {
        List<String> list = List.of(sourceUrl.split("/"));
        return list.get(list.size() - 4);
    }
}