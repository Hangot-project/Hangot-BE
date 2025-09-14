package com.hanyang.api.autocomplete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteQueryService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTOCOMPLETE_PREFIX = "autocomplete:";
    private static final int MAX_SUGGESTIONS = 5;

    public List<String> getSuggestions(String type, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return new ArrayList<>();
        }

        final String normalizedPrefix = normalizeWord(prefix);
        String key = AUTOCOMPLETE_PREFIX + type;

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Limit limit = Limit.limit().count(MAX_SUGGESTIONS);
        Range<String> range = Range.from(Range.Bound.inclusive("[" + normalizedPrefix)).to(Range.Bound.unbounded());

        Set<Object> rawResults = zSetOps.rangeByLex(key, range,limit);

        if (rawResults == null || rawResults.isEmpty()) {
            return new ArrayList<>();
        }

        return rawResults.stream()
                .map(Object::toString)
                .filter(word -> word.startsWith(normalizedPrefix))
                .toList();
    }

    public List<String> searchTags(String keyword) {
        return getSuggestions("tags", keyword);
    }

    public List<String> searchTitles(String keyword) {
        return getSuggestions("titles", keyword);
    }

    private String normalizeWord(String word) {
        return word.trim().toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}