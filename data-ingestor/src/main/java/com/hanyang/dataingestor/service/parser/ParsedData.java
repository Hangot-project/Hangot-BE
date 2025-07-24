package com.hanyang.dataingestor.service.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParsedData {
    private final List<String> header;
    private final List<List<String>> rows;
}