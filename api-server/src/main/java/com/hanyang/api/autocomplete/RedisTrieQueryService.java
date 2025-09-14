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

        Set<Object> completeWords = redisTemplate.opsForSet().members(hashKey + ":" + prefix + COMPLETE_SUFFIX);
        if (completeWords != null && !completeWords.isEmpty()) {
            suggestions.addAll(completeWords.stream()
                    .map(Object::toString)
                    .limit(MAX_SUGGESTIONS - suggestions.size())
                    .toList());
        }

        if (suggestions.size() >= MAX_SUGGESTIONS) {
            return;
        }

        Set<Object> nextChars = redisTemplate.opsForSet().members(hashKey + ":" + prefix + NODE_SUFFIX);
        if (nextChars != null && !nextChars.isEmpty()) {
            for (Object nextCharObj : nextChars) {
                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    break;
                }
                String nextChar = nextCharObj.toString();
                String newPrefix = prefix + nextChar;
                collectSuggestions(type, newPrefix, suggestions, depth + 1);
            }
        }
    }


    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}