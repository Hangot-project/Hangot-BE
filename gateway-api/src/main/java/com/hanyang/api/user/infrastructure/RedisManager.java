package com.hanyang.api.user.infrastructure;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisManager {
    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOperations;
}
