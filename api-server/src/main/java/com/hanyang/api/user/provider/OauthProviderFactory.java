package com.hanyang.api.user.provider;

import com.hanyang.api.core.exception.IllegalProviderException;
import com.hanyang.api.core.response.ResponseMessage;
import com.hanyang.api.user.repository.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OauthProviderFactory {
    private final List<OauthProvider> providers;

    public OauthProvider getProvider(String provider) {
        Provider providerEnum = Provider.valueOf(provider.toUpperCase());
        return providers.stream()
                .filter(p -> p.getProvider().equals(providerEnum))
                .findFirst()
                .orElseThrow(() -> new IllegalProviderException(ResponseMessage.ILLEGAL_PROVIDER));
    }
}
