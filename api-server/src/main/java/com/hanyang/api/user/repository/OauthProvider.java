package com.hanyang.api.user.repository;

import com.hanyang.api.user.provider.Provider;
import org.springframework.stereotype.Component;

@Component
public interface OauthProvider {
    String getProviderId(String code);
    Provider getProvider();
}
