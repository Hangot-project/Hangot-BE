package com.hanyang.dataingestor.service.parser;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvParser implements ParserStrategy {

    @Override
    public ParsedData parse(Path path, String datasetId) throws Exception {
        List<String> header = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(bufferedReader)) {
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