package com.hanyang.api.user.service;

import com.hanyang.api.core.jwt.component.JwtTokenProvider;
import com.hanyang.api.core.jwt.dto.TokenDto;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.dto.ResLoginDto;
import com.hanyang.api.user.infrastructure.RedisManager;
import com.hanyang.api.user.provider.OauthProviderFactory;
import com.hanyang.api.user.repository.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SocialLoginService {
    private final OauthProviderFactory oauthProviderFactory;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisManager redisManager;

    public ResLoginDto socialLogin(String provider, String code) {
        OauthProvider oauthProvider = oauthProviderFactory.getProvider(provider);
        String providerId = oauthProvider.getProviderId(code);
        User user = userService.signUp(oauthProvider.getProvider(), providerId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getProviderId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );

        TokenDto tokenDto = jwtTokenProvider.generateLoginToken(authentication, false);
        
        redisManager.setCode(user.getProviderId(), tokenDto.getRefreshToken(),
                86400000L);

        return ResLoginDto.builder()
                .grantType("Bearer")
                .accessToken(tokenDto.getAccessToken())
                .role(user.getRole().name())
                .build();
    }
}