package com.hanyang.datastore.service;

import com.hanyang.datastore.infrastructure.MongoManager;
import com.hanyang.datastore.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataParsingService {

    private final S3StorageManager s3StorageManager;
    private final MongoManager mongoManager;

    public void createDataTable(String datasetId) {

        InputStream file = s3StorageManager.getFile(datasetId);
        ExcelSheetHandler excelSheetHandler = ExcelSheetHandler.readExcel(file);

        mongoManager.createCollection(datasetId);

        List<String> headers = excelSheetHandler.getHeader();
        String[] columns = headers.toArray(new String[0]);

        int BATCH_SIZE = 1000;
        int rowCount = 0;

        List<List<String>> rows = excelSheetHandler.getRows();
        List<Map<String, Object>> buffer = new ArrayList<>();

        for (List<String> row : rows) {
            rowCount++;
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("_id", rowCount);
            for (int j = 0; j < row.size(); j++) {
                String cellValue = row.get(j);
                Object value;
                try {
                    value = Double.parseDouble(cellValue);
                } catch (NumberFormatException e) {
                    value = cellValue;
                }
                if (j < columns.length) {
                    document.put(columns[j], value);
                }
            }

            if (!document.isEmpty()) buffer.add(document);
            if (buffer.size() == BATCH_SIZE) {
                mongoManager.insertDocuments(datasetId, buffer);
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) mongoManager.insertDocuments(datasetId, buffer);
    }
}
