package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.infrastructure.MongoManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class FileHandlerFactory {

    private final MongoManager mongoManager;

    public FileDataHandler createHandler(String fileName, InputStream inputStream, String datasetId) {
        String extension = getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "xlsx", "xls", "xlsm", "xlsb", "xltx", "xltm" -> new ExcelSheetHandler(mongoManager, datasetId, inputStream);
            case "csv" -> CsvHandler.readCsv(inputStream);
            default -> throw new IllegalArgumentException(fileName + "은 지원하지 않는 파일 형식입니다.");
        };
    }

    private static String getFileExtension(String fileName) {
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