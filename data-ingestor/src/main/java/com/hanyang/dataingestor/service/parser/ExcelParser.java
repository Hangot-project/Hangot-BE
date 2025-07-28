package com.hanyang.dataingestor.service.parser;

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
import java.util.List;

@Component
public class ExcelParser implements ParserStrategy, XSSFSheetXMLHandler.SheetContentsHandler {

    private final ThreadLocal<List<String>> header = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<List<String>>> rows = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> currentRow = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<Integer> headerRowIndex = ThreadLocal.withInitial(() -> -1);
    private final ThreadLocal<Integer> checkedCol = ThreadLocal.withInitial(() -> -1);

    @Override
    public ParsedData parse(Path path, String datasetId) throws Exception {
        initializeFields();
        
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
            
            return new ParsedData(header.get(), rows.get());
        } finally {
            clearThreadLocalValues();
        }
    }

    private void initializeFields() {
        header.get().clear();
        rows.get().clear();
        currentRow.get().clear();
        headerRowIndex.set(-1);
        checkedCol.set(-1);
    }

    private void clearThreadLocalValues() {
        header.remove();
        rows.remove();
        currentRow.remove();
        headerRowIndex.remove();
        checkedCol.remove();
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
        List<List<String>> rowsList = rows.get();
        
        if (headerRowIndex.get() == -1 && !currentRowList.isEmpty() && 
            currentRowList.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
            headerRowIndex.set(rowNum);
            headerList.clear();
            headerList.addAll(currentRowList);
        } else if (headerRowIndex.get() != -1 && rowNum > headerRowIndex.get()) {
            while (currentRowList.size() < headerList.size()) {
                currentRowList.add("");
            }
            rowsList.add(new ArrayList<>(currentRowList));
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