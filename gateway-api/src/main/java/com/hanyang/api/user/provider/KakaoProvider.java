package com.hanyang.api.user.provider;

import com.hanyang.api.user.dto.OauthUserDto;
import com.hanyang.api.user.repository.OauthProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KakaoProvider implements OauthProvider {
    private final String provider = Provider.KAKAO.value();

    @Value("${kakao.rest_api_key}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @Value("${kakao.token_url}")
    private String token_url;

    @Value("${kakao.userInfo_url}")
    private String userInfo_url;

    @Value("${kakao.client_secret}")
    private String client_secret;

    private final JSONParser jsonParser = new JSONParser();

    @Override
    public OauthUserDto getUserInfo(final String code) {
        try {
            String accessToken = getAccessToken(code);

            WebClient webClient = WebClient.create(userInfo_url);
            String response = webClient.get()
                    .headers(httpHeaders -> {
                        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
                        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // parsing response to JSON
            JSONObject responseObj = (JSONObject) jsonParser.parse(response);
            JSONObject properties = (JSONObject) responseObj.get("properties");

            // parsing required values
            String userId = responseObj.get("id").toString();
            String name = properties.get("nickname").toString();

            return OauthUserDto.builder()
                    .email(userId)
                    .password(userId)
                    .name(name)
                    .build();
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String getProvider() {
        return provider;
    }

    private String getAccessToken(String code) throws ParseException {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", client_id);
        formData.add("redirect_uri", redirect_uri);
        formData.add("code", code);
        formData.add("client_secret", client_secret);

        WebClient webClient = WebClient.create(token_url);
        String response = webClient.post()
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData(formData))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        clientResponse -> clientResponse.bodyToMono(String.class).map(Exception::new)
                )
                .bodyToMono(String.class)
                .block();
        JSONObject responseObj = (JSONObject) jsonParser.parse(response);
        return responseObj.get("access_token").toString();
    }
}
