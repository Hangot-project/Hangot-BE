package com.hanyang.dataingestor.service;

import java.io.InputStream;

public class FileHandlerFactory {

    public static FileDataHandler createHandler(String fileName, InputStream inputStream) {
        String extension = getFileExtension(fileName);
        
        switch (extension.toLowerCase()) {
            case "xlsx":
            case "xls":
                return ExcelSheetHandler.readExcel(inputStream);
            case "csv":
                return CsvHandler.readCsv(inputStream);
            default:
                throw new IllegalArgumentException();
        }
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