package com.hanyang.dataingestor.service.parser;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvParser implements ParserStrategy {

    @Override
    public ParsedData parse(InputStream inputStream, String datasetId) throws Exception {
        List<String> header = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8))) {
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
    }
}