package com.hanyang.api.user.repository;

import com.hanyang.api.user.dto.OauthUserDto;
import org.springframework.stereotype.Component;

@Component
public interface OauthProvider {
    OauthUserDto getUserInfo(String code);
    String getProvider();
}
