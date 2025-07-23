package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.InvalidFileFormatException;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.opencsv.CSVReader;
import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CsvHandler implements FileDataHandler {

    private final List<String> header = new ArrayList<>();

    private final List<List<String>> rows = new ArrayList<>();

    public static CsvHandler readCsv(InputStream inputStream) {
        CsvHandler csvHandler = new CsvHandler();
        
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            
            Charset detectedCharset = detectCharset(bufferedInputStream);
            
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(bufferedInputStream, detectedCharset))) {
                String[] nextRecord;
                boolean isFirstRow = true;

                while ((nextRecord = csvReader.readNext()) != null) {
                    if (isFirstRow) {
                        csvHandler.header.addAll(Arrays.asList(nextRecord));
                        isFirstRow = false;
                    } else {
                        csvHandler.rows.add(Arrays.asList(nextRecord));
                    }
                }
            }

        } catch (Exception e) {
            throw new InvalidFileFormatException("CSV 파일 읽기 실패", e);
        }

        return csvHandler;
    }
    
    private static Charset detectCharset(BufferedInputStream inputStream) throws Exception {
        inputStream.mark(8192);
        
        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(inputStream);
            
            CharsetMatch match = detector.detect();
            String detectedEncoding = match.getName();
            

            if ("ISO-8859-1".equals(detectedEncoding) || "windows-1252".equals(detectedEncoding)) {
                return Charset.forName("EUC-KR");
            }
            
            if ("UTF-8".equals(detectedEncoding)) {
                return StandardCharsets.UTF_8;
            }
            
            if ("EUC-KR".equals(detectedEncoding) || "x-windows-949".equals(detectedEncoding)) {
                return Charset.forName("EUC-KR");
            }
            
            return StandardCharsets.UTF_8;
            
        } finally {
            inputStream.reset();
        }
    }
}