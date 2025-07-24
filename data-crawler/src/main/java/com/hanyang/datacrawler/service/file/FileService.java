package com.hanyang.datacrawler.service.file;

import com.hanyang.datacrawler.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileService {
    
    @Value("${data.batch.size}")
    private int batchSize;
    
    private final S3StorageManager s3StorageManager;
    private final CsvHandler csvHandler;
    private final ExcelHandler excelHandler;
    
    public void processFileInChunks(String folderName, Path filePath) {
        // 해당 폴더 내 모든 파일 삭제
        s3StorageManager.deleteAllFilesInFolder(folderName);
        
        FileType fileType = FileType.getFileType(filePath.getFileName().toString());
        FileStrategy strategy = getProcessingStrategy(fileType);
        strategy.processFile(folderName, filePath, batchSize);
    }
    
    private FileStrategy getProcessingStrategy(FileType fileType) {
        return switch (fileType) {
            case CSV -> csvHandler;
            case XLS, XLSX -> excelHandler;
            default -> throw new UnsupportedOperationException("지원하지 않는 파일 형식: " + fileType);
        };
    }
}