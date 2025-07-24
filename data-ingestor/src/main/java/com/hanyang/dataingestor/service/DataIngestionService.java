package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.infrastructure.MongoManager;
import com.hanyang.dataingestor.infrastructure.S3StorageManager;
import com.hanyang.dataingestor.service.parser.ParsedData;
import com.hanyang.dataingestor.service.parser.ParserStrategy;
import com.hanyang.dataingestor.service.parser.ParsingStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private final S3StorageManager s3StorageManager;
    private final ParsingStrategyResolver parsingStrategyResolver;
    private final MongoManager mongoManager;


    public void createDataTable(String datasetId){
        String fileName = s3StorageManager.getFirstFileName(datasetId);
        mongoManager.createCollection(datasetId);

        s3StorageManager.processFiles(datasetId, file -> {
            ParserStrategy strategy = parsingStrategyResolver.getStrategy(fileName);
            try {
                ParsedData parsedData = strategy.parse(file, datasetId);

                if (!parsedData.getHeader().isEmpty() && !parsedData.getRows().isEmpty()) {
                    String[] columns = parsedData.getHeader().toArray(new String[0]);
                    mongoManager.insertDataRows(datasetId, columns, parsedData.getRows());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
