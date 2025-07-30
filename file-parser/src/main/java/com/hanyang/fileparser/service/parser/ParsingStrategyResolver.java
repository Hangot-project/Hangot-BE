package com.hanyang.fileparser.service.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParsingStrategyResolver {

    private final CsvParser csvParser;
    private final ExcelParser excelParser;

    public ParserStrategy getStrategy(String type) {
        return switch (type) {
            case "CSV" -> csvParser;
            case "XLSX" -> excelParser;
            default -> throw new IllegalArgumentException(type + "은 지원하지 않는 파일 형식입니다.");
        };
    }
}