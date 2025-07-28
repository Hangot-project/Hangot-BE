package com.hanyang.dataingestor.service.parser;

import java.nio.file.Path;

public interface ParserStrategy {
    ParsedData parse(Path path, String datasetId) throws Exception;
}