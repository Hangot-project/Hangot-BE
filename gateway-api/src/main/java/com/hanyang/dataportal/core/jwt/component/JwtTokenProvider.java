package com.hanyang.dataportal.core.jwt.component;

import com.hanyang.dataportal.core.jwt.dto.TokenDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtSecretKey jwtSecretKey;

    public static final Long SESSION_COOKIE_MAX_AGE = -1L;
    public static final String REFRESH_COOKIE_KEY = "refreshToken";
    public static final String AUTO_LOGIN_CLAIM_KEY = "auto";

    @Value("${jwt.expire.refresh}")
    private Long refreshExpire;
    @Value("${jwt.expire.access}")
    private Long accessExpire;

    /**
     * JWT 토큰 생성
     * @param authentication
     * @param isAutoLogin 자동로그인 여부
     * @param expiredInMillisecond 토큰 만료시간(밀리초)
     * @return
     */
    private String generateToken(final Authentication authentication, final boolean isAutoLogin, final Long expiredInMillisecond) {
        // role 가져오기
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        final long now = (new Date()).getTime();
        final Date accessTokenExpiresIn = new Date(now + expiredInMillisecond);

        // Access Token 생성
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim(AUTO_LOGIN_CLAIM_KEY, isAutoLogin)
                .setExpiration(accessTokenExpiresIn)
                .signWith(jwtSecretKey.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

//    private String generateRefreshToken(
//            final Authentication authentication,
//            final boolean isAutoLogin,
//            final Long expiredInMillisecond
//    ) {
//        return generateToken(authentication, isAutoLogin, expiredInMillisecond);
//        redisManager.setCode(authentication.getName(), refreshToken, expiredInMillisecond);
//    }

    /**
     * 액세스 토큰과 리프레시 토큰을 새로 발급하는 메서드
     * @param authentication
     * @param isAutoLogin null 값 방지를 위해 boolean 타입으로 지정
     * @return
     */
    public TokenDto generateLoginToken(final Authentication authentication, final boolean isAutoLogin) {
        final String accessToken = generateToken(authentication, isAutoLogin, accessExpire);
        final String refreshToken = generateToken(authentication, isAutoLogin, refreshExpire);
        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * refresh token 쿠키를 생성하는 메서드
     * @param refreshToken 쿠키로 발급할 리프레시 토큰 값
     * @param autoLogin 자동로그인 여부. 리프레시 토큰 쿠키 삭제시 null로 입력된다.
     * @return
     */
    public ResponseCookie generateRefreshCookie(final String refreshToken, @Nullable final Boolean autoLogin) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(REFRESH_COOKIE_KEY, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .domain(".hy-data.site");
        // logout
        if (autoLogin == null) {
            cookieBuilder.maxAge(0);
        } else if (!autoLogin) {
            cookieBuilder.maxAge(SESSION_COOKIE_MAX_AGE);
        } else {
            final Duration duration = Duration.ofMillis(refreshExpire);
            cookieBuilder.maxAge(duration);
        }
        return cookieBuilder.build();
    }
}