package com.hanyang.api.user.service;

import com.hanyang.api.core.jwt.JwtTokenProvider;
import com.hanyang.api.user.domain.User;
import com.hanyang.api.user.dto.ResLoginDto;
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

    public ResLoginDto socialLogin(java.lang.String provider, java.lang.String code) {
        OauthProvider oauthProvider = oauthProviderFactory.getProvider(provider);
        java.lang.String providerId = oauthProvider.getProviderId(code);
        User user = userService.signUp(oauthProvider.getProvider(), providerId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getProviderId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );

         String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        return ResLoginDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .role(user.getRole().name())
                .build();
    }
}