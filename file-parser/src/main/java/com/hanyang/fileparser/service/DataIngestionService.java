package com.hanyang.fileparser.service;

import com.hanyang.fileparser.core.exception.ParsingException;
import com.hanyang.fileparser.core.exception.ResourceNotFoundException;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.infrastructure.MongoManager;
import com.hanyang.fileparser.service.parser.FileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private static final int CHUNK_SIZE = 10000;

    private final FileParser fileParser;
    private final MongoManager mongoManager;
    private final FileService fileService;

    public void createDataTable(MessageDto messageDto) throws ResourceNotFoundException,IllegalArgumentException, ParsingException {
        mongoManager.dropCollection(messageDto.getDatasetId());

        if(messageDto.getResourceUrl() == null || messageDto.getResourceUrl().isEmpty()){
            throw new ResourceNotFoundException("파일 다운로드 링크가 없습니다.");
        }

        Path resourcePath = fileService.downloadFile(messageDto.getResourceUrl(), messageDto.getType());

        try {
            final String[][] columns = new String[1][];
            
            fileParser.parse(
                resourcePath, 
                messageDto.getDatasetId(),
                header -> columns[0] = header.toArray(new String[0]),
                chunk -> mongoManager.insertDataRows(messageDto.getDatasetId(), columns[0], chunk),
                CHUNK_SIZE
            );
        } finally {
            try {
                Files.deleteIfExists(resourcePath);
            } catch (Exception e) {
                log.warn("임시파일 삭제 실패: {}", resourcePath, e);
            }
        }
    }
}
