package com.hanyang.dataingestor.service.parser;

import com.hanyang.dataingestor.core.exception.InvalidFileFormatException;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvParser implements ParserStrategy {

    @Override
    public ParsedData parse(InputStream inputStream, String datasetId) {
        List<String> header = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            
            Charset detectedCharset = detectCharset(bufferedInputStream);

            try (CSVReader csvReader = new CSVReader(new InputStreamReader(bufferedInputStream, detectedCharset))) {
                String[] nextRecord;
                boolean isFirstRow = true;

                while ((nextRecord = csvReader.readNext()) != null) {
                    if (isFirstRow) {
                        header.addAll(Arrays.asList(nextRecord));
                        isFirstRow = false;
                    } else {
                        rows.add(Arrays.asList(nextRecord));
                    }
                }
            }

            return new ParsedData(header, rows);

        } catch (Exception e) {
            throw new InvalidFileFormatException("CSV 파일 읽기 실패", e);
        }
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

            if ("EUC-KR".equals(detectedEncoding) || "x-windows-949".equals(detectedEncoding) || "CP949".equals(detectedEncoding)) {
                return Charset.forName("EUC-KR");
            }
            
            return StandardCharsets.UTF_8;
            
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        } finally {
            inputStream.reset();
        }
    }
}