package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.UnAuthenticationException;
import com.hanyang.api.core.jwt.component.JwtTokenProvider;
import com.hanyang.api.user.infrastructure.RedisManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLogoutService {
    private final RedisManager redisManager;
    private final JwtTokenProvider jwtTokenProvider;

    public void logout(UserDetails userDetails) {
        if (userDetails == null) {
           throw new UnAuthenticationException("로그인 상태가 아닙니다.");
        }
        redisManager.deleteCode(userDetails.getUsername());
    }

    public ResponseCookie generateRefreshCookie(final String refreshToken) {
        return jwtTokenProvider.generateRefreshCookie(refreshToken, null);
    }
}
