package com.hanyang.api.user.controller;

import com.hanyang.api.user.dto.ResLoginDto;
import com.hanyang.api.user.service.SocialLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "유저 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/login")
public class UserLoginController {
    private final SocialLoginService socialLoginService;
    
    @Value("${app.client.domain}")
    private String clientDomain;
    
    @Value("${app.client.redirect-url}")
    private String redirectUrl;

    @Operation(summary = "소셜 로그인")
    @GetMapping("/{provider}")
    public void socialLogin(
            @PathVariable String provider,
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        ResLoginDto dto = socialLoginService.socialLogin(provider, code);
        
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", dto.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .domain(clientDomain)
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.sendRedirect(redirectUrl);
    }

}
