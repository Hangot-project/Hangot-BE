package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.DataProcessingException;
import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import com.hanyang.dataingestor.infrastructure.S3StorageManager;
import com.hanyang.dataingestor.service.strategy.ParsingStrategy;
import com.hanyang.dataingestor.service.strategy.ParsingStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataParsingService {

    private final S3StorageManager s3StorageManager;
    private final ParsingStrategyResolver parsingStrategyResolver;


    public void createDataTable(String datasetId) {
        InputStream file = s3StorageManager.getFile(datasetId);
        if (file == null) {
            throw new ResourceNotFoundException("파일을 찾을 수 없습니다: datasetId - " + datasetId);
        }

        String fileName = s3StorageManager.getFirstFileName(datasetId);
        if (fileName == null) {
            throw new DataProcessingException("파일명을 가져올 수 없습니다: datasetId - " + datasetId);
        }

        ParsingStrategy strategy = parsingStrategyResolver.getStrategy(fileName);
        strategy.parse(file, datasetId);
    }
}
