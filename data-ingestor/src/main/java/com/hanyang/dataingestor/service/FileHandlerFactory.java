package com.hanyang.dataingestor.service;

import java.io.InputStream;

public class FileHandlerFactory {

    public static FileDataHandler createHandler(String fileName, InputStream inputStream) {
        String extension = getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "xlsx", "xls", "xlsm", "xlsb", "xltx", "xltm" -> ExcelSheetHandler.readExcel(inputStream);
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