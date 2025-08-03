package com.hanyang.api.autocomplete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTrieQueryService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TRIE_PREFIX = "trie:";
    private static final String NODE_SUFFIX = ":nodes";
    private static final String COMPLETE_SUFFIX = ":complete";
    private static final String DELIMITER = ",";
    private static final int MAX_SUGGESTIONS = 5;
    
    public List<String> getSuggestions(String type, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        prefix = normalizeWord(prefix);
        
        List<String> suggestions = new ArrayList<>();
        collectSuggestions(type, prefix, suggestions, 0);
        
        return suggestions.stream()
                .distinct()
                .limit(MAX_SUGGESTIONS)
                .sorted()
                .collect(Collectors.toList());
    }
    
    private void collectSuggestions(String type, String prefix, List<String> suggestions, int depth) {
        if (suggestions.size() >= MAX_SUGGESTIONS || depth > 20) {
            return;
        }
        
        String hashKey = TRIE_PREFIX + type;
        
        String completeWordsStr = (String) redisTemplate.opsForHash().get(hashKey, prefix + COMPLETE_SUFFIX);
        if (completeWordsStr != null && !completeWordsStr.isEmpty()) {
            List<String> completeWords = List.of(completeWordsStr.split(DELIMITER));
            suggestions.addAll(completeWords.stream()
                    .limit(MAX_SUGGESTIONS - suggestions.size())
                    .toList());
        }
        
        if (suggestions.size() >= MAX_SUGGESTIONS) {
            return;
        }
        
        String nextCharsStr = (String) redisTemplate.opsForHash().get(hashKey, prefix + NODE_SUFFIX);
        if (nextCharsStr != null && !nextCharsStr.isEmpty()) {
            List<String> nextChars = List.of(nextCharsStr.split(DELIMITER));
            for (String nextChar : nextChars) {
                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    break;
                }
                String newPrefix = prefix + nextChar;
                collectSuggestions(type, newPrefix, suggestions, depth + 1);
            }
        }
    }
    
    public boolean isEmpty(String type) {
        String hashKey = TRIE_PREFIX + type;
        Boolean hasKey = redisTemplate.hasKey(hashKey);
        return hasKey == null || !hasKey;
    }
    
    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}