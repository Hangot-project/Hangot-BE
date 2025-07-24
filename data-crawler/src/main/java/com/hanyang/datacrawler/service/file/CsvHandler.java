package com.hanyang.datacrawler.service.file;

import com.hanyang.datacrawler.infrastructure.S3StorageManager;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvHandler implements FileStrategy {
    
    private final S3StorageManager s3StorageManager;
    
    @Override
    public void processFile(String folderName, Path filePath, int batchSize) {
        try {
            processFileWithChunking(folderName, filePath, batchSize);
        } catch (Exception e) {
            throw new RuntimeException("CSV 파일 처리 실패", e);
        }
    }
    
    private void processFileWithChunking(String folderName, Path filePath, int batchSize) throws Exception {
        List<String> header = new ArrayList<>();
        List<List<String>> currentChunk = new ArrayList<>();
        int chunkIndex = 0;
        boolean headerProcessed = false;
        
        Charset[] charsetsToTry = {
            StandardCharsets.UTF_8,
            Charset.forName("EUC-KR"),
            StandardCharsets.ISO_8859_1
        };
        
        Exception lastException = null;
        
        for (Charset charset : charsetsToTry) {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath))) {
                Charset detectedCharset = detectCharset(inputStream);
                
                // 먼저 감지된 인코딩 시도, 실패하면 순차적으로 시도
                Charset charsetToUse = (charset == charsetsToTry[0]) ? detectedCharset : charset;
                
                try (BufferedReader reader = Files.newBufferedReader(filePath, charsetToUse)) {
                    String line;
                    header.clear();
                    currentChunk.clear();
                    chunkIndex = 0;
                    headerProcessed = false;

                    while ((line = reader.readLine()) != null) {
                        String[] cells = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        List<String> row = new ArrayList<>();
                        
                        for (String cell : cells) {
                            if (cell.startsWith("\"") && cell.endsWith("\"") && cell.length() > 1) {
                                cell = cell.substring(1, cell.length() - 1);
                            }
                            row.add(cell.trim());
                        }
                        
                        if (!headerProcessed) {
                            header.addAll(row);
                            headerProcessed = true;
                        } else {
                            while (row.size() < header.size()) {
                                row.add("");
                            }
                            currentChunk.add(row);
                            
                            if (currentChunk.size() >= batchSize) {
                                saveChunkToCsv(folderName, header, currentChunk, chunkIndex);
                                currentChunk.clear();
                                chunkIndex++;
                            }
                        }
                    }
                    
                    if (!currentChunk.isEmpty()) {
                        saveChunkToCsv(folderName, header, currentChunk, chunkIndex);
                    }
                    
                    // 성공적으로 처리되면 반복 중단
                    return;
                    
                } catch (Exception e) {
                    lastException = e;
                    continue; // 다음 인코딩 시도
                }
            } catch (Exception e) {
                lastException = e;
                continue; // 다음 인코딩 시도
            }
        }
        
        // 모든 인코딩 시도 실패
        throw new RuntimeException("모든 인코딩으로 파일 읽기 실패", lastException);
    }
    
    private void saveChunkToCsv(String folderName, List<String> header, List<List<String>> currentChunk, int chunkIndex) {
        if (header.isEmpty() || currentChunk.isEmpty()) {
            return;
        }
        
        try {
            StringBuilder csvContent = new StringBuilder();
            
            csvContent.append(String.join(",", header)).append("\n");
            
            for (List<String> row : currentChunk) {
                csvContent.append(String.join(",", row)).append("\n");
            }
            
            String chunkFileName = chunkIndex + ".csv";
            String s3ObjectPath = folderName + "/" + chunkFileName;
            
            byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);
            s3StorageManager.uploadFile(s3ObjectPath, csvBytes, "text/csv; charset=UTF-8");
            
        } catch (Exception e) {
            throw new RuntimeException("CSV 청크 저장 실패", e);
        }
    }
    
    private static Charset detectCharset(BufferedInputStream inputStream) throws Exception {
        inputStream.mark(8192);

        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(inputStream);

            CharsetMatch match = detector.detect();
            if (match == null) {
                return StandardCharsets.UTF_8;
            }
            
            String detectedEncoding = match.getName();
            int confidence = match.getConfidence();
            
            // 신뢰도가 낮으면 UTF-8 사용
            if (confidence < 50) {
                return StandardCharsets.UTF_8;
            }

            if ("ISO-8859-1".equals(detectedEncoding) || "windows-1252".equals(detectedEncoding)) {
                return Charset.forName("EUC-KR");
            }

            if ("UTF-8".equals(detectedEncoding)) {
                return StandardCharsets.UTF_8;
            }

            if ("EUC-KR".equals(detectedEncoding) || "x-windows-949".equals(detectedEncoding) || "CP949".equals(detectedEncoding)) {
                return Charset.forName("EUC-KR");
            }

            return StandardCharsets.UTF_8;

        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        } finally {
            inputStream.reset();
        }
    }
}