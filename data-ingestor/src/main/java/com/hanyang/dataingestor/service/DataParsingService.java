package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.infrastructure.MongoManager;
import com.hanyang.dataingestor.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataParsingService {

    private final S3StorageManager s3StorageManager;
    private final MongoManager mongoManager;
    
    @Value("${datastore.batch.size:1000}")
    private int batchSize;
    
    private static final String ID_FIELD = "_id";

    public void createDataTable(String datasetId,String resourceUrl) {
        try (InputStream file = s3StorageManager.getFile(datasetId)) {
            if (file == null) {
                log.error("파일을 찾을 수 없습니다: datasetId - {}", datasetId);
                return;
            }
            
            String fileName = s3StorageManager.getFirstFileName(datasetId);
            if (fileName == null) {
                log.error("파일명을 가져올 수 없습니다: datasetId - {}", datasetId);
                return;
            }
            
            FileDataHandler fileHandler = FileHandlerFactory.createHandler(fileName, file);
            
            mongoManager.createCollection(datasetId);
            
            processFileData(datasetId, fileHandler);
            
        }catch (IllegalArgumentException e){
            log.info("지원 하지 않는 파일 형식: resourceUrl - {} errorMessage - {}", resourceUrl,e.getMessage());
        }
        catch (Exception e) {
            log.error("데이터 파싱 실패: resourceUrl - {} errorMessage - {}", resourceUrl, e.getMessage());
            cleanupOnFailure(datasetId);
        }
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
        
        processBatchData(datasetId, columns, rows);
    }

    private boolean validateHeaders(String datasetId, List<String> headers) {
        if (headers.isEmpty()) {
            log.error("헤더가 없습니다: {}", datasetId);
            return false;
        }
        return true;
    }

    private void processBatchData(String datasetId, String[] columns, List<List<String>> rows) {
        List<Map<String, Object>> buffer = new ArrayList<>();
        int rowCount = 0;
        
        for (List<String> row : rows) {
            rowCount++;
            Map<String, Object> document = createDocument(row, columns, rowCount);
            
            if (!document.isEmpty()) {
                buffer.add(document);
                
                if (buffer.size() >= batchSize) {
                    mongoManager.insertDocuments(datasetId, buffer);
                    buffer.clear();
                }
            }
        }
        
        if (!buffer.isEmpty()) {
            mongoManager.insertDocuments(datasetId, buffer);
        }
        
        log.info("데이터 저장 완료: {} 건", rowCount);
    }

    private Map<String, Object> createDocument(List<String> row, String[] columns, int rowId) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put(ID_FIELD, rowId);
        
        for (int j = 0; j < row.size() && j < columns.length; j++) {
            String cellValue = row.get(j);
            Object value = parseValue(cellValue);
            document.put(columns[j], value);
        }
        
        return document;
    }

    private Object parseValue(String cellValue) {
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return "";
        }
        
        try {
            // 정수인지 확인
            if (!cellValue.contains(".")) {
                return Long.parseLong(cellValue);
            }
            // 실수인지 확인
            return Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            return cellValue.trim();
        }
    }

    private void cleanupOnFailure(String datasetId) {
        try {
            mongoManager.dropIfExists(datasetId);
        } catch (Exception e) {
            log.error("컬렉션 정리 실패: {}", datasetId);
        }
    }
}
