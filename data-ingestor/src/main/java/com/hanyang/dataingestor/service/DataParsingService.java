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
    private final FileHandlerFactory fileHandlerFactory;
    

    public void createDataTable(String datasetId) {
        InputStream file = s3StorageManager.getFile(datasetId);
        if (file == null) {
            throw new ResourceNotFoundException("파일을 찾을 수 없습니다: datasetId - " + datasetId);
        }

        String fileName = s3StorageManager.getFirstFileName(datasetId);
        if (fileName == null) {
            throw new DataProcessingException("파일명을 가져올 수 없습니다: datasetId - " + datasetId);
        }

        mongoManager.createCollection(datasetId);
        
        FileDataHandler fileHandler = fileHandlerFactory.createHandler(fileName, file, datasetId);
        processFileData(datasetId, fileHandler);
    }

    private void processFileData(String datasetId, FileDataHandler fileHandler) {
        List<String> headers = fileHandler.getHeader();
        
        if (!validateHeaders(datasetId, headers)) {
            return;
        }
        
        List<List<String>> rows = fileHandler.getRows();
        if (!rows.isEmpty()) {
            String[] columns = headers.toArray(new String[0]);
            mongoManager.insertDataRows(datasetId, columns, rows);
        }
    }

    private boolean validateHeaders(String datasetId, List<String> headers) {
        if (headers.isEmpty()) {
            log.error("헤더가 없습니다: {}", datasetId);
            return false;
        }
        return true;
    }
}
