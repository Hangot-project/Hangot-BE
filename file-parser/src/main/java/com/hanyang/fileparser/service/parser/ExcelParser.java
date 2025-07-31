package com.hanyang.fileparser.service.parser;

import com.hanyang.fileparser.core.exception.ParsingException;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
public class ExcelParser implements ParserStrategy, XSSFSheetXMLHandler.SheetContentsHandler {

    private final ThreadLocal<List<String>> header = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<List<String>>> tempChunk = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> currentRow = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<Integer> headerRowIndex = ThreadLocal.withInitial(() -> -1);
    private final ThreadLocal<Integer> checkedCol = ThreadLocal.withInitial(() -> -1);
    private final ThreadLocal<Consumer<List<String>>> headerCallback = new ThreadLocal<>();
    private final ThreadLocal<Consumer<List<List<String>>>> chunkCallback = new ThreadLocal<>();
    private final ThreadLocal<Integer> chunkSize = new ThreadLocal<>();
    private final ThreadLocal<Boolean> headerSent = ThreadLocal.withInitial(() -> false);

    @Override
    public void parse(Path path, String datasetId, Consumer<List<String>> headerCallback,
                                 Consumer<List<List<String>>> chunkCallback, int chunkSize) throws ParsingException {
        initializeFields();
        this.headerCallback.set(headerCallback);
        this.chunkCallback.set(chunkCallback);
        this.chunkSize.set(chunkSize);
        
        try {
            try (OPCPackage opcPackage = OPCPackage.open(path.toFile())) {
                XSSFReader xssfReader = new XSSFReader(opcPackage);
                StylesTable stylesTable = xssfReader.getStylesTable();
                ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
                
                try (InputStream sheetStream = xssfReader.getSheetsData().next()) {
                    InputSource sheetSource = new InputSource(sheetStream);
                    
                    ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, this, false);
                    XMLReader sheetParser = XMLHelper.newXMLReader();
                    
                    sheetParser.setContentHandler(handler);
                    sheetParser.parse(sheetSource);
                }
            }
            
            List<List<String>> currentTempChunk = tempChunk.get();
            if (!currentTempChunk.isEmpty()) {
                this.chunkCallback.get().accept(new ArrayList<>(currentTempChunk));
            }
            
        } catch (Exception e) {
            throw new ParsingException(Arrays.toString(e.getStackTrace()));
        } finally {
            clearThreadLocalValues();
        }
    }

    private void initializeFields() {
        header.get().clear();
        tempChunk.get().clear();
        currentRow.get().clear();
        headerRowIndex.set(-1);
        checkedCol.set(-1);
        headerSent.set(false);
    }

    private void clearThreadLocalValues() {
        header.remove();
        tempChunk.remove();
        currentRow.remove();
        headerRowIndex.remove();
        checkedCol.remove();
        headerCallback.remove();
        chunkCallback.remove();
        chunkSize.remove();
        headerSent.remove();
    }

    @Override
    public void startRow(int rowNum) {
        currentRow.get().clear();
        checkedCol.set(-1);
    }

    @Override
    public void endRow(int rowNum) {
        List<String> currentRowList = currentRow.get();
        List<String> headerList = header.get();
        List<List<String>> tempChunkList = tempChunk.get();
        
        if (headerRowIndex.get() == -1 && !currentRowList.isEmpty() && 
            currentRowList.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
            headerRowIndex.set(rowNum);
            headerList.clear();
            headerList.addAll(currentRowList);
            
            if (!headerSent.get()) {
                headerCallback.get().accept(new ArrayList<>(headerList));
                headerSent.set(true);
            }
            
        } else if (headerRowIndex.get() != -1 && rowNum > headerRowIndex.get()) {
            while (currentRowList.size() < headerList.size()) {
                currentRowList.add("");
            }
            tempChunkList.add(new ArrayList<>(currentRowList));
            
            if (tempChunkList.size() >= chunkSize.get()) {
                chunkCallback.get().accept(new ArrayList<>(tempChunkList));
                tempChunkList.clear();
            }
        }
    }

    @Override
    public void cell(String cellReference, String value, XSSFComment comment) {
        int currentCol = new CellReference(cellReference).getCol();
        int emptyColumnCount = currentCol - checkedCol.get() - 1;
        List<String> currentRowList = currentRow.get();
        
        for (int i = 0; i < emptyColumnCount; i++) {
            currentRowList.add("");
        }
        
        currentRowList.add(value != null ? value : "");
        checkedCol.set(currentCol);
    }
}