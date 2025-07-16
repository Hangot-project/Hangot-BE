package com.hanyang.api.user.infrastructure;

import com.hanyang.api.core.exception.UnAuthenticationException;
import com.hanyang.api.core.response.ResponseMessage;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisManager {
    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOperations;

    public void setCode(String email,String code){
        valueOperations.set(email,code,180, TimeUnit.SECONDS);
    }
    public void setCode(String email, String code, Long expire) {
        valueOperations.set(email, code, expire, TimeUnit.MILLISECONDS);
    }

    public String getCode(String email){
        Object code = valueOperations.get(email);
        if(code == null){
            throw new UnAuthenticationException(ResponseMessage.UN_AUTHORIZED);
        }
        return code.toString();
    }

    public void deleteCode(String email) {
        getCode(email);
        valueOperations.getAndDelete(email);
    }

    public void setDataExpire(String key, String value, Duration duration) {
        valueOperations.set(key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public String getData(String key) {
        Object data = valueOperations.get(key);
        return data != null ? data.toString() : null;
    }

    public void deleteData(String key) {
        valueOperations.getAndDelete(key);
    }
}
