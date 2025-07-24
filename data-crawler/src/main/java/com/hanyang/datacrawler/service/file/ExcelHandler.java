package com.hanyang.datacrawler.service.file;

import com.hanyang.datacrawler.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelHandler implements FileStrategy, XSSFSheetXMLHandler.SheetContentsHandler {
    
    private final S3StorageManager s3StorageManager;
    
    private String folderName;
    private int batchSize;
    private final List<String> header = new ArrayList<>();
    private final List<List<String>> currentChunk = new ArrayList<>();
    private final List<String> currentRow = new ArrayList<>();
    
    private int headerRowIndex = -1;
    private int checkedCol = -1;
    private int chunkIndex = 0;
    
    @Override
    public void processFile(String folderName, Path filePath, int batchSize) {
        this.folderName = folderName;
        this.batchSize = batchSize;
        
        try {
            processExcelWithChunking(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Excel 파일 처리 실패", e);
        }
    }
    
    private void processExcelWithChunking(Path filePath) throws Exception {
        try (OPCPackage opcPackage = OPCPackage.open(filePath.toFile())) {
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable stylesTable = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
            
            try (InputStream sheetStream = xssfReader.getSheetsData().next()) {
                InputSource sheetSource = new InputSource(sheetStream);
                
                ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, this, false);
                XMLReader sheetParser = XMLHelper.newXMLReader();
                
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);
                
                finalizeChunks();
            }
        }
    }
    
    @Override
    public void startRow(int rowNum) {
        this.checkedCol = -1;
    }
    
    @Override
    public void endRow(int rowNum) {
        if (headerRowIndex == -1 && !currentRow.isEmpty() && 
            currentRow.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
            headerRowIndex = rowNum;
            header.clear();
            header.addAll(currentRow);
        } else if (headerRowIndex != -1 && rowNum > headerRowIndex) {
            while (currentRow.size() < header.size()) {
                currentRow.add("");
            }
            currentChunk.add(new ArrayList<>(currentRow));
            
            if (currentChunk.size() >= batchSize) {
                saveChunkToCsv();
                currentChunk.clear();
                chunkIndex++;
            }
        }
        currentRow.clear();
    }
    
    @Override
    public void cell(String cellReference, String value, XSSFComment comment) {
        int currentCol = new CellReference(cellReference).getCol();
        int emptyColumnCount = currentCol - checkedCol - 1;
        
        for (int i = 0; i < emptyColumnCount; i++) {
            currentRow.add("");
        }
        
        currentRow.add(value != null ? value : "");
        checkedCol = currentCol;
    }
    
    private void finalizeChunks() {
        if (!currentChunk.isEmpty()) {
            saveChunkToCsv();
        }
    }
    
    private void saveChunkToCsv() {
        if (header.isEmpty() || currentChunk.isEmpty()) {
            return;
        }
        
        try {
            StringBuilder csvContent = new StringBuilder();
            
            csvContent.append(String.join(",", header)).append("\n");
            
            for (List<String> row : currentChunk) {
                csvContent.append(String.join(",", row)).append("\n");
            }
            
            String chunkFileName = "chunk_" + chunkIndex + ".csv";
            String s3ObjectPath = folderName + "/" + chunkFileName;
            
            byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);
            s3StorageManager.uploadFile(s3ObjectPath, csvBytes, "text/csv; charset=UTF-8");
            
        } catch (Exception e) {
            throw new RuntimeException("CSV 청크 저장 실패", e);
        }
    }
}