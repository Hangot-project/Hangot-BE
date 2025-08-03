package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.repository.DatasetRepository;
import com.hanyang.datacrawler.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteUpdateService implements ApplicationRunner {
    private final RedisTrieService redisTrieService;
    private final DatasetRepository datasetRepository;
    private final TagRepository tagRepository;
    
    private static final String TAG_TYPE = "tags";
    private static final String TITLE_TYPE = "titles";
    
    @Override
    public void run(ApplicationArguments args) {
        initializeTrieData();
    }
    
    public void initializeTrieData() {

        List<String> tags = tagRepository.findAllDistinctTags();
        List<String> titles = datasetRepository.findAllDistinctTitles();

        redisTrieService.refreshTrieData(TAG_TYPE, tags);
        redisTrieService.refreshTrieData(TITLE_TYPE, titles);
    }
    
    public void updateAfterDatasetSave() {
        initializeTrieData();
    }
}