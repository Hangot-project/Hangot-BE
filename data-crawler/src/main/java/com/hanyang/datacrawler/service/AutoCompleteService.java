package com.hanyang.datacrawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoCompleteService {
    private final RedisTrieService redisTrieService;
    
    private static final String TAG_TYPE = "tags";
    private static final String TITLE_TYPE = "titles";
    

    public void addTitleAndTag(List<String> newTitles, List<String> newTags) {
        if (newTitles != null && !newTitles.isEmpty()) {
            redisTrieService.addWordsIncremental(TITLE_TYPE, newTitles);
        }
        if (newTags != null && !newTags.isEmpty()) {
            redisTrieService.addWordsIncremental(TAG_TYPE, newTags);
        }
    }
}