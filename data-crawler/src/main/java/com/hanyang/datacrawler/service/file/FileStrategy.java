package com.hanyang.datacrawler.service.file;

import java.nio.file.Path;

public interface FileStrategy {
    void processFile(String folderName, Path filePath, int batchSize);
}