package com.hanyang.api.user.service;

import com.hanyang.api.core.exception.ResourceExistException;
import com.hanyang.api.core.jwt.dto.TokenDto;
import com.hanyang.api.user.dto.req.ReqLoginDto;
import com.hanyang.api.user.dto.req.ReqSignupDto;
import com.hanyang.api.user.dto.OauthUserDto;
import com.hanyang.api.user.repository.OauthProvider;
import com.hanyang.api.user.provider.OauthProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthLoginService {
    private final OauthProviderFactory oauthProviderFactory;
    private final UserLoginService userLoginService;
    private final UserService userService;

    public TokenDto login(final String provider, final String code) {
        OauthProvider oauthProvider = oauthProviderFactory.getProvider(provider);
        OauthUserDto userInfo = oauthProvider.getUserInfo(code);
        findOrSignup(userInfo);
        return userLoginService.login((new ReqLoginDto()).convertFrom(userInfo));
    }

    private void findOrSignup(OauthUserDto userDto) {
        try {
            userService.signUp((new ReqSignupDto().convertFrom(userDto)));
        } catch (ResourceExistException e) { }
    }
}
