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

    public void createDataTable(String datasetId) {
        log.info("데이터 테이블 생성 시작: datasetId={}", datasetId);
        
        try (InputStream file = s3StorageManager.getFile(datasetId)) {
            ExcelSheetHandler excelSheetHandler = ExcelSheetHandler.readExcel(file);
            
            mongoManager.createCollection(datasetId);
            
            processExcelData(datasetId, excelSheetHandler);
            
            log.info("데이터 테이블 생성 완료: datasetId={}", datasetId);
            
        } catch (Exception e) {
            log.error("데이터 테이블 생성 실패: datasetId={}", datasetId, e);
            // 실패 시 생성된 컬렉션 정리
            cleanupOnFailure(datasetId);
            throw new RuntimeException("데이터 파싱 중 오류가 발생했습니다", e);
        }
    }

    private void processExcelData(String datasetId, ExcelSheetHandler excelSheetHandler) {
        List<String> headers = excelSheetHandler.getHeader();
        validateHeaders(headers);
        
        String[] columns = headers.toArray(new String[0]);
        List<List<String>> rows = excelSheetHandler.getRows();
        
        if (rows.isEmpty()) {
            log.warn("처리할 데이터가 없습니다: datasetId={}", datasetId);
            return;
        }
        
        processBatchData(datasetId, columns, rows);
    }

    private void validateHeaders(List<String> headers) {
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("헤더가 없습니다");
        }
        
        if (headers.contains(ID_FIELD)) {
            throw new IllegalArgumentException("헤더에 예약어 '_id'가 포함되어 있습니다");
        }
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
                    log.debug("배치 처리 완료: {} rows", rowCount);
                }
            }
        }
        
        // 남은 데이터 처리
        if (!buffer.isEmpty()) {
            mongoManager.insertDocuments(datasetId, buffer);
            log.debug("최종 배치 처리 완료: {} rows", rowCount);
        }
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
            log.info("실패한 컬렉션 정리 완료: datasetId={}", datasetId);
        } catch (Exception e) {
            log.error("컬렉션 정리 실패: datasetId={}", datasetId, e);
        }
    }
}
