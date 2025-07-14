package com.hanyang.api.core.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.hanyang.api.core.response.ResponseMessage.UN_AUTHORIZED;

/**
 * 인증이 안된 사용자(JWT 존재 X) 401 에러 처리
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiResponseBuilder apiResponseBuilder;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
       apiResponseBuilder.buildErrorResponse(response,HttpStatus.UNAUTHORIZED, UN_AUTHORIZED);
    }

}
