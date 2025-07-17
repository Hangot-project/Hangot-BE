package com.hanyang.api.user.provider;

import com.hanyang.api.user.repository.OauthProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoProvider implements OauthProvider {
    private final Provider provider = Provider.KAKAO;

    @Value("${kakao.rest_api_key}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @Value("${kakao.token_url}")
    private String token_url;

    @Value("${kakao.userInfo_url}")
    private String userInfo_url;

    private final JSONParser jsonParser = new JSONParser();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getProviderId(final String code) {
        String accessToken = getAccessToken(code);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                userInfo_url,
                HttpMethod.GET,
                entity,
                String.class
        );

        // parsing response to JSON
        JSONObject responseObj;
        try {
            responseObj = (JSONObject) jsonParser.parse(response.getBody());
        } catch (ParseException e) {
            throw new RuntimeException("카카오 로그인 유저 정보 파싱 실패");
        }

        return responseObj.get("id").toString();
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    private String getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", client_id);
        formData.add("redirect_uri", redirect_uri);
        formData.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                token_url,
                HttpMethod.POST,
                entity,
                String.class
        );

        JSONObject responseObj;
        try {
            responseObj = (JSONObject) jsonParser.parse(response.getBody());
        } catch (ParseException e) {
            throw new RuntimeException("카카오 로그인 토큰 파싱 실페");
        }
        return responseObj.get("access_token").toString();
    }
}
