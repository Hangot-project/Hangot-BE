package com.hanyang.fileparser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatasetCountService {
    
    private static final String PROCESSING_COUNT_KEY_PREFIX = "processing_count:";
    
    private final StringRedisTemplate redisTemplate;
    
    public void incrementProcessingCount(String datasetId) {
        String key = PROCESSING_COUNT_KEY_PREFIX + datasetId;
        redisTemplate.opsForValue().increment(key);
    }
    
    public void removeKey(String datasetId) {
        String key = PROCESSING_COUNT_KEY_PREFIX + datasetId;
        redisTemplate.delete(key);
    }
    
    public int getCurrentProcessingCount(String datasetId) {
        String key = PROCESSING_COUNT_KEY_PREFIX + datasetId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 1;
    }
    
    public int calculateDynamicChunkSize(String datasetId, int baseChunkSize) {
        int processingCount = getCurrentProcessingCount(datasetId);
        
        if (processingCount <= 1) {
            return baseChunkSize;
        }
        
        int adjustedChunkSize = baseChunkSize / (processingCount / 2 + 1);
        
        return Math.max(adjustedChunkSize, 1000);
    }
}