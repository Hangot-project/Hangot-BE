package com.hanyang.api.core.jwt.component;

import com.hanyang.api.core.exception.JwtTokenException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.hanyang.api.core.response.ResponseMessage.INVALID_JWT;

@Component
@RequiredArgsConstructor
public class JwtTokenResolver {

    private final JwtSecretKey jwtSecretKey;

    /**
     * 액세스 토큰을 복호화하는 메서드
     * @param accessToken
     * @return
     */
    private Claims parseToken(final String accessToken) throws ExpiredJwtException, MalformedJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey.getKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    /**
     * 클레임에서 권한 정보를 꺼내오는 메서드
     * @param claims
     * @return
     */
    private Collection<? extends GrantedAuthority> getAuthorities(final Claims claims) {
        return Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 복호화한 액세스 토큰을 검증하는 메서드
     * @param claims
     */
    private void validateClaims(Claims claims) {
        if (claims.get("auth") == null) {
            throw new JwtTokenException(INVALID_JWT);
        }
    }

    /**
     * JWT 토큰을 복호화하여 토큰에 들어있는 유저 정보를 꺼내는 메서드
     * @param accessToken
     * @return
     */
    public Authentication getAuthentication(final String accessToken) {
        try {
            final Claims claims = parseToken(accessToken);
            validateClaims(claims);

            final Collection<? extends GrantedAuthority> authorities = getAuthorities(claims);

            UserDetails principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, "", authorities);
        } catch (ExpiredJwtException e) {
            final Collection<? extends GrantedAuthority> authorities = getAuthorities(e.getClaims());
            UserDetails principal = new User(e.getClaims().getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, "", authorities);
        } catch (MalformedJwtException e) {
            throw new JwtTokenException("토큰 형식이 올바르지 않습니다.");
        }
    }

    /**
     * 액세스 토큰에서 자동로그인 여부를 추출하는 메서드
     * @param accessToken
     * @return
     */
    public boolean getAutoLogin(final String accessToken) {
        try {
            final Claims claims = parseToken(accessToken);
            validateClaims(claims);
            return (Boolean) claims.get(JwtTokenProvider.AUTO_LOGIN_CLAIM_KEY);
        } catch (ExpiredJwtException e) {
            return (Boolean) e.getClaims().get(JwtTokenProvider.AUTO_LOGIN_CLAIM_KEY);
        }
    }
}
