package com.hanyang.datastore.service;

import com.hanyang.datastore.core.exception.InvalidFileFormatException;
import lombok.Getter;
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

public class ExcelSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    @Getter
    private List<String> header = new ArrayList<>();

    @Getter
    private List<List<String>> rows = new ArrayList<>();

    private List<String> row = new ArrayList<>();

    private int checkedCol = -1;

    @Override
    public void startRow(int currentRowNum) {
        this.checkedCol = -1;
    }

    @Override
    public void endRow(int currentRowNum) {
        //헤더 보다 이전
        int START_ROW_NUM = 1;
        if (currentRowNum < START_ROW_NUM - 1) return;
        //헤더
        else if (currentRowNum == START_ROW_NUM - 1) {
            header = new ArrayList<>(row);
        }
        //비어 있는 셀
        else {
            if (row.size() < header.size()) {
                for (int i = row.size(); i < header.size(); i++) {
                    row.add("");
                }
            }
            rows.add(new ArrayList<>(row));
        }
        row.clear();
    }


    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
        XSSFSheetXMLHandler.SheetContentsHandler.super.headerFooter(text, isHeader, tagName);
    }

    @Override
    public void endSheet() {
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

    public static ExcelSheetHandler readExcel(InputStream inputStream) {
        ExcelSheetHandler excelSheetHandler = new ExcelSheetHandler();

        try {
            OPCPackage opcPackage = OPCPackage.open(inputStream);

            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable stylesTable = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);

            InputStream sheetStream = xssfReader.getSheetsData().next();
            InputSource sheetSource = new InputSource(sheetStream);

            ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, excelSheetHandler, false);
            XMLReader sheetParser = XMLHelper.newXMLReader();

            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);

            sheetStream.close();
        } catch (Exception e) {
            throw new InvalidFileFormatException(e.getMessage());
        }

        return excelSheetHandler;
    }
}
