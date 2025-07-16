package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.ResourceExistException;
import com.hanyang.api.core.jwt.component.JwtTokenProvider;
import com.hanyang.api.core.jwt.dto.TokenDto;
import com.hanyang.api.user.dto.OauthUserDto;
import com.hanyang.api.user.repository.OauthProvider;
import com.hanyang.api.user.provider.OauthProviderFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthLoginService {
    private final OauthProviderFactory oauthProviderFactory;
    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenDto login(final String provider, final String code) {
        OauthProvider oauthProvider = oauthProviderFactory.getProvider(provider);
        OauthUserDto userInfo = oauthProvider.getUserInfo(code);
        findOrSignup(userInfo);
        
        // OAuth 사용자 로그인 처리
        final UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(userInfo.getEmail(), "");
        final Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        
        // JWT 토큰 생성 (자동로그인 false로 설정)
        return jwtTokenProvider.generateLoginToken(authentication, false);
    }

    private void findOrSignup(OauthUserDto userDto) {
        try {
            userService.signUpOauth(userDto);
        } catch (ResourceExistException e) { 
            // 이미 존재하는 사용자인 경우 무시
        }
    }
}
