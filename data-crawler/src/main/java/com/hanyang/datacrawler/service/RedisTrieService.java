package com.hanyang.datacrawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTrieService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TRIE_PREFIX = "trie:";
    private static final String NODE_SUFFIX = ":nodes";
    private static final String COMPLETE_SUFFIX = ":complete";
    private static final String DELIMITER = ",";
    
    public void addWord(String type, String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        word = normalizeWord(word);
        String hashKey = TRIE_PREFIX + type;
        
        for (int i = 0; i <= word.length(); i++) {
            String prefix = word.substring(0, i);
            
            if (i < word.length()) {
                char nextChar = word.charAt(i);
                addToHashSet(hashKey, prefix + NODE_SUFFIX, String.valueOf(nextChar));
            } else {
                addToHashSet(hashKey, prefix + COMPLETE_SUFFIX, word);
            }
        }
    }
    
    private void addToHashSet(String hashKey, String field, String value) {
        String existing = (String) redisTemplate.opsForHash().get(hashKey, field);
        Set<String> values = new HashSet<>();
        
        if (existing != null && !existing.isEmpty()) {
            values.addAll(Arrays.asList(existing.split(DELIMITER)));
        }
        values.add(value);
        
        redisTemplate.opsForHash().put(hashKey, field, String.join(DELIMITER, values));
    }
    
    public void addWords(String type, List<String> words) {
        words.forEach(word -> addWord(type, word));
    }
    
    public void clearTrie(String type) {
        String hashKey = TRIE_PREFIX + type;
        redisTemplate.delete(hashKey);
    }
    
    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
    
    public void refreshTrieData(String type, List<String> words) {
        clearTrie(type);
        addWords(type, words);
    }
}