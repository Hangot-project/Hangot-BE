package com.hanyang.dataportal.user.provider;

import com.hanyang.dataportal.core.exception.IllegalProviderException;
import com.hanyang.dataportal.core.response.ResponseMessage;
import com.hanyang.dataportal.user.repository.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OauthProviderFactory {
    private final List<OauthProvider> providers;

    public OauthProvider getProvider(String provider) {
        return providers.stream()
                .filter(p -> p.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalProviderException(ResponseMessage.ILLEGAL_PROVIDER));
    }
}
