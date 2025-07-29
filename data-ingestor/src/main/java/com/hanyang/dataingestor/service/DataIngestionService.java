package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.infrastructure.MongoManager;
import com.hanyang.dataingestor.service.parser.ParsedData;
import com.hanyang.dataingestor.service.parser.ParserStrategy;
import com.hanyang.dataingestor.service.parser.ParsingStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private final ParsingStrategyResolver parsingStrategyResolver;
    private final MongoManager mongoManager;
    private final FileService fileService;

    public void createDataTable(MessageDto messageDto) throws Exception {
        mongoManager.dropCollection(messageDto.getDatasetId());

        if(messageDto.getResourceUrl() == null || messageDto.getResourceUrl().isEmpty()){
            throw new ResourceNotFoundException("파일 다운로드 링크가 없습니다.");
        }

        ParserStrategy strategy = parsingStrategyResolver.getStrategy(messageDto.getType());

        Path resourcePath = fileService.downloadFile(messageDto.getResourceUrl(), messageDto.getType());

        ParsedData parsedData = strategy.parse(resourcePath,messageDto.getDatasetId());
        String[] columns = parsedData.getHeader().toArray(new String[0]);
        mongoManager.insertDataRows(messageDto.getDatasetId(), columns, parsedData.getRows());
    }
}
