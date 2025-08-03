package com.hanyang.api.autocomplete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteQueryService {
    private final RedisTrieQueryService redisTrieQueryService;

    private static final String TAG_TYPE = "tags";
    private static final String TITLE_TYPE = "titles";
    
    public List<String> searchTags(String query) {
        return redisTrieQueryService.getSuggestions(TAG_TYPE, query);
    }
    
    public List<String> searchTitles(String query) {
        if (query == null) return List.of();
        String processedQuery = query.trim().replace(" ", "").replace("_", "");
        if (processedQuery.isEmpty()) return List.of();
        return redisTrieQueryService.getSuggestions(TITLE_TYPE, processedQuery);
    }
}