package com.hanyang.dataingestor.service.parser;

import com.hanyang.dataingestor.core.exception.ParsingException;

import java.nio.file.Path;

public interface ParserStrategy {
    ParsedData parse(Path path, String datasetId) throws ParsingException;
}