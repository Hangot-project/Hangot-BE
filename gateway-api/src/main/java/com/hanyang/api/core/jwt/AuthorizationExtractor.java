package com.hanyang.api.core.jwt;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationExtractor {
    public static final String AUTH_TYPE = "Bearer";

    /**
     * 인증 헤더 필드 값에서 액세스 토큰을 추출하여 반환하는 메서드
     * @param authorizationHeader 헤더의 Authorization 필드 값
     * @return
     */
    @Nullable
    public static String extractAccessToken(final String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTH_TYPE)) {
            return authorizationHeader.substring(AUTH_TYPE.length()).trim();
        }
        return null;
    }
}
