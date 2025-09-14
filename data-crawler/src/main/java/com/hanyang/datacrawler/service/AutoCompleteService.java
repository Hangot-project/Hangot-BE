package com.hanyang.datacrawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoCompleteService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTOCOMPLETE_PREFIX = "autocomplete:";
    private static final String TAG_TYPE = "tags";
    private static final String TITLE_TYPE = "titles";

    public void addWord(String type, String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }

        word = normalizeWord(word);
        String key = AUTOCOMPLETE_PREFIX + type;

        redisTemplate.opsForZSet().add(key, word, 0);
    }
    public void addWordsIncremental(String type, List<String> words) {
        words.forEach(word -> addWord(type, word));
    }

    public void addTitleAndTag(List<String> newTitles, List<String> newTags) {
        if (newTitles != null && !newTitles.isEmpty()) {
            addWordsIncremental(TITLE_TYPE, newTitles);
        }
        if (newTags != null && !newTags.isEmpty()) {
            addWordsIncremental(TAG_TYPE, newTags);
        }
    }

    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }


}