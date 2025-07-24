package com.hanyang.dataingestor.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParsingStrategyResolver {

    private final ExcelParsingStrategy excelParsingStrategy;
    private final CsvParsingStrategy csvParsingStrategy;

    public ParsingStrategy getStrategy(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "xlsx", "xls", "xlsm", "xlsb", "xltx", "xltm" -> excelParsingStrategy;
            case "csv" -> csvParsingStrategy;
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