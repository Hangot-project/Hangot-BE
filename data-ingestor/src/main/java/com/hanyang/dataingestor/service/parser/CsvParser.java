package com.hanyang.dataingestor.service.parser;

import com.hanyang.dataingestor.core.exception.ParsingException;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvParser implements ParserStrategy {


    @Override
    public ParsedData parse(Path path, String datasetId) throws ParsingException {
        List<String> header = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            Charset detectedCharset = detectCharset(inputStream);
            
            InputStreamReader reader = new InputStreamReader(inputStream, detectedCharset);
            BufferedReader bufferedReader = new BufferedReader(reader);
            CSVReader csvReader = new CSVReader(bufferedReader);

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
        } catch (Exception e) {
            throw new ParsingException(Arrays.toString(e.getStackTrace()));
        }

        return new ParsedData(header, rows);
    }

    private Charset detectCharset(BufferedInputStream inputStream) throws IOException {
        inputStream.mark(8192);

        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(inputStream);

            CharsetMatch match = detector.detect();
            if (match == null) {
                return StandardCharsets.UTF_8;
            }

            String detectedEncoding = match.getName();
            int confidence = match.getConfidence();

            // 신뢰도가 낮으면 UTF-8 사용
            if (confidence < 50) {
                return StandardCharsets.UTF_8;
            }

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