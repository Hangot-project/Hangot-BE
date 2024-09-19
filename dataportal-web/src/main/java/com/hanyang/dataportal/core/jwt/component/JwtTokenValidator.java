package com.hanyang.dataportal.core.jwt.component;

import com.hanyang.dataportal.core.exception.UnAuthenticationException;
import com.hanyang.dataportal.user.infrastructure.RedisManager;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtSecretKey jwtSecretKey;
//    private final RedisManager redisManager;

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey.getKey()).build().parseClaimsJws(token);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

//    /**
//     * 유저가 가진 리프레시 토큰이 유효한지 검증하는 메서드
//     * @param email
//     * @param refreshToken
//     * @return
//     */
//    public boolean validateRefreshToken(final String email, final String refreshToken) {
//        if (!validateToken(refreshToken)) return false;
//        try {
//            return redisManager.getCode(email).equals(refreshToken);
//        } catch (UnAuthenticationException e) {
//            return false;
//        }
//    }
}
