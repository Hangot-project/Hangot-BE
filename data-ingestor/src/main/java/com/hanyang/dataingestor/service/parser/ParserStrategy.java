package com.hanyang.dataingestor.service.parser;

import java.io.InputStream;

public interface ParserStrategy {
    ParsedData parse(InputStream inputStream, String datasetId) throws Exception;
}