package com.hanyang.datacrawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTrieService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TRIE_PREFIX = "trie:";
    private static final String NODE_SUFFIX = ":nodes";
    private static final String COMPLETE_SUFFIX = ":complete";
    
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
        redisTemplate.opsForSet().add(hashKey + ":" + field, value);
    }
    
    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }

    public void addWordsIncremental(String type, List<String> words) {
        words.forEach(word -> addWord(type, word));
    }

}