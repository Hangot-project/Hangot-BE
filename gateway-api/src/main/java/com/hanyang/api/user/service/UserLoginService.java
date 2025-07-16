package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.TokenExpiredException;
import com.hanyang.api.core.jwt.component.JwtTokenProvider;
import com.hanyang.api.core.jwt.component.JwtTokenResolver;
import com.hanyang.api.core.jwt.component.JwtTokenValidator;
import com.hanyang.api.core.jwt.dto.TokenDto;
import com.hanyang.api.core.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLoginService {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenResolver jwtTokenResolver;

    public TokenDto reissueToken(final String refreshToken) throws TokenExpiredException {
        final Authentication authentication = jwtTokenResolver.getAuthentication(refreshToken);
        final boolean autoLogin = jwtTokenResolver.getAutoLogin(refreshToken);
        if (jwtTokenValidator.validateToken(refreshToken)) {
            return jwtTokenProvider.generateLoginToken(authentication, autoLogin);
        }
        throw new TokenExpiredException(ResponseMessage.REFRESH_EXPIRED);
    }

    public ResponseCookie generateRefreshCookie(final TokenDto tokenDto) {
        return jwtTokenProvider.generateRefreshCookie(
                tokenDto.getRefreshToken(),
                jwtTokenResolver.getAutoLogin(tokenDto.getAccessToken())
        );
    }
}
