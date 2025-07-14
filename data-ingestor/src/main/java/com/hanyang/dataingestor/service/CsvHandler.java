package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.InvalidFileFormatException;
import com.opencsv.CSVReader;
import lombok.Getter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvHandler implements FileDataHandler {

    @Getter
    private final List<String> header = new ArrayList<>();

    @Getter
    private final List<List<String>> rows = new ArrayList<>();

    public static CsvHandler readCsv(InputStream inputStream) {
        CsvHandler csvHandler = new CsvHandler();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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

        } catch (Exception e) {
            throw new InvalidFileFormatException("CSV 파일 읽기 실패", e);
        }

        return csvHandler;
    }
}