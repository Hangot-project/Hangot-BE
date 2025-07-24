package com.hanyang.dataingestor.service.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParsingStrategyResolver {

    private final ExcelParser excelParser;
    private final CsvParser csvParser;

    public ParserStrategy getStrategy(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "xlsx", "xls", "xlsm", "xlsb", "xltx", "xltm" -> excelParser;
            case "csv" -> csvParser;
            default -> throw new IllegalArgumentException(fileName + "은 지원하지 않는 파일 형식입니다.");
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
}