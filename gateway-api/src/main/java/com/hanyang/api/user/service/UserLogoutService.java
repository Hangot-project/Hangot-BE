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

    /**
     * 로그인 유저를 로그아웃 처리하는 메서드 (redis 내 해당 유저의 칼럼 삭제)
     * @param userDetails
     */
    public void logout(UserDetails userDetails) {
        if (userDetails == null) {
           throw new UnAuthenticationException("로그인 상태가 아닙니다.");
        }
        redisManager.deleteCode(userDetails.getUsername());
    }

    /**
     * refresh token 삭제 쿠키를 생성하는 메서드
     * @param refreshToken
     * @return
     */
    public ResponseCookie generateRefreshCookie(final String refreshToken) {
        return jwtTokenProvider.generateRefreshCookie(refreshToken, null);
    }
}
