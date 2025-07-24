package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.InvalidFileFormatException;
import com.hanyang.dataingestor.infrastructure.MongoManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler, FileDataHandler {

    private static final int HEADER_ROW_INDEX = 0;
    
    @Value("${data.batch.size:1000}")
    private int batchSize;

    @Getter
    private final List<String> header = new ArrayList<>();

    @Getter
    private final List<List<String>> rows = new ArrayList<>();

    private final List<String> row = new ArrayList<>();
    private final MongoManager mongoManager;
    private final String datasetId;

    private int checkedCol = -1;

    public ExcelSheetHandler(MongoManager mongoManager, String datasetId, InputStream inputStream) {
        this.mongoManager = mongoManager;
        this.datasetId = datasetId;
        parseExcel(inputStream);
    }

    @Override
    public void startRow(int currentRowNum) {
        this.checkedCol = -1;
    }

    @Override
    public void endRow(int currentRowNum) {
        if (currentRowNum < HEADER_ROW_INDEX) {
            return;
        }

        if (currentRowNum == HEADER_ROW_INDEX) {
            header.clear();
            header.addAll(row);
        } else {
            // 빈 셀 채우기
            while (row.size() < header.size()) {
                row.add("");
            }
            rows.add(new ArrayList<>(row));
            
            // 배치 처리
            if (mongoManager != null && rows.size() >= batchSize) {
                processBatch();
            }
        }

        row.clear();
    }

    private void processBatch() {
        if (!header.isEmpty() && !rows.isEmpty()) {
            String[] columns = header.toArray(new String[0]);
            mongoManager.insertDataRows(datasetId, columns, new ArrayList<>(rows));
            rows.clear();
        }
    }


    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        XSSFSheetXMLHandler.SheetContentsHandler.super.headerFooter(text, isHeader, tagName);
    }

    @Override
    public void endSheet() {
        // 남은 데이터 처리
        if (mongoManager != null && !rows.isEmpty()) {
            processBatch();
        }
        XSSFSheetXMLHandler.SheetContentsHandler.super.endSheet();
    }

    @Override
    public void cell(String columnName, String value, XSSFComment comment) {
        int currentCol = new CellReference(columnName).getCol();
        int emptyColumnCount = currentCol - checkedCol - 1;

        for (int i = 0; i < emptyColumnCount; i++) {
            row.add("");
        }

        row.add(value);
        checkedCol = currentCol;
    }

    private void parseExcel(InputStream inputStream) {
        try {
            OPCPackage opcPackage = OPCPackage.open(inputStream);

            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable stylesTable = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);

            InputStream sheetStream = xssfReader.getSheetsData().next();
            InputSource sheetSource = new InputSource(sheetStream);

            ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, this, false);
            XMLReader sheetParser = XMLHelper.newXMLReader();

            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);

            sheetStream.close();
        } catch (Exception e) {
            throw new InvalidFileFormatException("엑셀 파일 읽기 실패", e);
        }
    }
}
