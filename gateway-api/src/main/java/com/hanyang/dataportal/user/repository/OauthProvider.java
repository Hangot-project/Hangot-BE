package com.hanyang.dataportal.user.repository;

import com.hanyang.dataportal.user.dto.OauthUserDto;
import org.springframework.stereotype.Component;

@Component
public interface OauthProvider {
    OauthUserDto getUserInfo(String code);
    String getProvider();
}
