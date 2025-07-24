package com.hanyang.dataingestor.service.strategy;

import com.hanyang.dataingestor.core.exception.InvalidFileFormatException;
import com.hanyang.dataingestor.infrastructure.MongoManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ExcelParsingStrategy implements ParsingStrategy, XSSFSheetXMLHandler.SheetContentsHandler {

    
    @Value("${data.batch.size}")
    private int batchSize;

    private final MongoManager mongoManager;

    private final List<String> header = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private final List<String> row = new ArrayList<>();
    
    private String datasetId;
    private int checkedCol = -1;
    private int headerRowIndex = -1;

    @Override
    public void parse(InputStream inputStream, String datasetId) {
        this.datasetId = datasetId;
        mongoManager.createCollection(datasetId);

        try (OPCPackage opcPackage = OPCPackage.open(inputStream)) {
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

        } catch (Exception e) {
            throw new InvalidFileFormatException("엑셀 파일 읽기 실패", e);
        }
    }

    @Override
    public void startRow(int currentRowNum) {
        this.checkedCol = -1;
    }

    @Override
    public void endRow(int currentRowNum) {
        // 첫 번째 데이터가 있는 행을 헤더로 설정
        if (headerRowIndex == -1 && !row.isEmpty() && row.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
            headerRowIndex = currentRowNum;
            header.clear();
            header.addAll(row);
        } else if (headerRowIndex != -1 && currentRowNum > headerRowIndex) {
            // 빈 셀 채우기
            while (row.size() < header.size()) {
                row.add("");
            }
            rows.add(new ArrayList<>(row));
            
            // 배치 처리후 rows 초기화
            if (rows.size() >= batchSize) {
                processBatch();
                rows.clear();
            }
        }
        row.clear();
    }

    @Override
    public void endSheet() {
        // 남은 데이터 처리
        if (!rows.isEmpty()) {
            processBatch();
            rows.clear();
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

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        XSSFSheetXMLHandler.SheetContentsHandler.super.headerFooter(text, isHeader, tagName);
    }

    private void processBatch() {
        if (!header.isEmpty() && !rows.isEmpty()) {
            String[] columns = header.toArray(new String[0]);
            mongoManager.insertDataRows(datasetId, columns, rows);
        }
    }
}