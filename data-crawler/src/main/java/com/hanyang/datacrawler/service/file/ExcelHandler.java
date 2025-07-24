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
    
    private final ThreadLocal<String> folderName = new ThreadLocal<>();
    private final ThreadLocal<Integer> batchSize = new ThreadLocal<>();
    private final ThreadLocal<List<String>> header = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<List<String>>> currentChunk = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> currentRow = ThreadLocal.withInitial(ArrayList::new);

    private final ThreadLocal<Integer> headerRowIndex = ThreadLocal.withInitial(() -> -1);
    private final ThreadLocal<Integer> checkedCol = ThreadLocal.withInitial(() -> -1);
    private final ThreadLocal<Integer> chunkIndex = ThreadLocal.withInitial(() -> 0);

    
    @Override
    public void processFile(String folderName, Path filePath, int batchSize) {
        this.folderName.set(folderName);
        this.batchSize.set(batchSize);

        header.get().clear();
        currentChunk.get().clear();
        currentRow.get().clear();
        headerRowIndex.set(-1);
        checkedCol.set(-1);
        chunkIndex.set(0);

        try {
            processExcelWithChunking(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Excel 파일 처리 실패", e);
        } finally {
            clearThreadLocalValues();
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
        this.checkedCol.set(-1);
    }
    
    @Override
    public void endRow(int rowNum) {
        List<String> currentRowList = currentRow.get();
        List<String> headerList = header.get();
        List<List<String>> currentChunkList = currentChunk.get();
        
        if (headerRowIndex.get() == -1 && !currentRowList.isEmpty() && 
            currentRowList.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
            headerRowIndex.set(rowNum);
            headerList.clear();
            headerList.addAll(currentRowList);
        } else if (headerRowIndex.get() != -1 && rowNum > headerRowIndex.get()) {
            while (currentRowList.size() < headerList.size()) {
                currentRowList.add("");
            }
            currentChunkList.add(new ArrayList<>(currentRowList));
            
            if (currentChunkList.size() >= batchSize.get()) {
                saveChunkToCsv();
                currentChunkList.clear();
                chunkIndex.set(chunkIndex.get() + 1);
            }
        }
        currentRowList.clear();
    }
    
    @Override
    public void cell(String cellReference, String value, XSSFComment comment) {
        int currentCol = new CellReference(cellReference).getCol();
        int checkedColValue = checkedCol.get();
        int emptyColumnCount = currentCol - checkedColValue - 1;
        List<String> currentRowList = currentRow.get();
        
        for (int i = 0; i < emptyColumnCount; i++) {
            currentRowList.add("");
        }
        
        currentRowList.add(value != null ? value : "");
        checkedCol.set(currentCol);
    }
    
    private void finalizeChunks() {
        if (!currentChunk.get().isEmpty()) {
            saveChunkToCsv();
        }
    }
    
    private void saveChunkToCsv() {
        List<String> headerList = header.get();
        List<List<String>> currentChunkList = currentChunk.get();
        
        if (headerList.isEmpty() || currentChunkList.isEmpty()) {
            return;
        }
        
        try {
            StringBuilder csvContent = new StringBuilder();
            
            csvContent.append(String.join(",", headerList)).append("\n");
            
            for (List<String> row : currentChunkList) {
                csvContent.append(String.join(",", row)).append("\n");
            }
            
            String chunkFileName = chunkIndex.get() + ".csv";
            String s3ObjectPath = folderName.get() + "/" + chunkFileName;
            
            byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);
            s3StorageManager.uploadFile(s3ObjectPath, csvBytes, "text/csv; charset=UTF-8");
            
        } catch (Exception e) {
            throw new RuntimeException("CSV 청크 저장 실패", e);
        }
    }
    
    private void clearThreadLocalValues() {
        folderName.remove();
        batchSize.remove();
        header.remove();
        currentChunk.remove();
        currentRow.remove();
        headerRowIndex.remove();
        checkedCol.remove();
        chunkIndex.remove();
    }
}