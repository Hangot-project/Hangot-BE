package com.hanyang.api.core.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtSecretKey jwtSecretKey;

    public static final String AUTO_LOGIN_CLAIM_KEY = "auto";

    @Value("${jwt.expire.access}")
    private Long accessExpire;

    public String generateAccessToken(final Authentication authentication, final boolean isAutoLogin) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        final long now = (new Date()).getTime();
        final Date accessTokenExpiresIn = new Date(now + accessExpire);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim(AUTO_LOGIN_CLAIM_KEY, isAutoLogin)
                .setExpiration(accessTokenExpiresIn)
                .signWith(jwtSecretKey.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}