package com.hanyang.fileparser.service.parser;

import com.hanyang.fileparser.core.exception.ParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class FileParser {

    private final CsvParser csvParser;
    private final ExcelParser excelParser;

    public void parse(Path path, String datasetId, Consumer<List<String>> headerCallback,
                     Consumer<List<List<String>>> chunkCallback, int chunkSize) throws ParsingException {
        
        String fileExtension = getFileExtension(path);
        ParserStrategy strategy = getParserStrategy(fileExtension);
        
        strategy.parse(path, datasetId, headerCallback, chunkCallback, chunkSize);
    }

    private String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        System.out.println(fileName);
        int lastDotIndex = fileName.lastIndexOf('.');
        
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일 확장자를 찾을 수 없습니다: " + fileName);
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private ParserStrategy getParserStrategy(String extension) {
        return switch (extension) {
            case "csv" -> csvParser;
            case "xlsx", "xls" -> excelParser;
            default -> throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + extension);
        };
    }
}