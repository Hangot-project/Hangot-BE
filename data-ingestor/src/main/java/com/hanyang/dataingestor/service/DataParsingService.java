package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.DataProcessingException;
import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import com.hanyang.dataingestor.infrastructure.MongoManager;
import com.hanyang.dataingestor.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataParsingService {

    private final S3StorageManager s3StorageManager;
    private final MongoManager mongoManager;
    

    public void createDataTable(String datasetId) {
        InputStream file = s3StorageManager.getFile(datasetId);
        if (file == null) {
            throw new ResourceNotFoundException("파일을 찾을 수 없습니다: datasetId - " + datasetId);
        }

        String fileName = s3StorageManager.getFirstFileName(datasetId);
        if (fileName == null) {
            throw new DataProcessingException("파일명을 가져올 수 없습니다: datasetId - " + datasetId);
        }

        FileDataHandler fileHandler = FileHandlerFactory.createHandler(fileName, file);

        mongoManager.createCollection(datasetId);
        processFileData(datasetId, fileHandler);
    }

    private void processFileData(String datasetId, FileDataHandler fileHandler) {
        List<String> headers = fileHandler.getHeader();
        
        if (!validateHeaders(datasetId, headers)) {
            return;
        }
        
        String[] columns = headers.toArray(new String[0]);
        List<List<String>> rows = fileHandler.getRows();
        
        if (rows.isEmpty()) {
            log.warn("처리할 데이터가 없습니다: {}", datasetId);
            return;
        }
        
        mongoManager.processBatchData(datasetId, columns, rows);
    }

    private boolean validateHeaders(String datasetId, List<String> headers) {
        if (headers.isEmpty()) {
            log.error("헤더가 없습니다: {}", datasetId);
            return false;
        }
        return true;
    }
}
