package com.hanyang.dataingestor.service.strategy;

import java.io.InputStream;

public interface ParsingStrategy {
    void parse(InputStream inputStream, String datasetId);
}