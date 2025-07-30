package com.hanyang.fileparser.service.parser;

import com.hanyang.fileparser.core.exception.ParsingException;

import java.nio.file.Path;

public interface ParserStrategy {
    ParsedData parse(Path path, String datasetId) throws ParsingException;
}