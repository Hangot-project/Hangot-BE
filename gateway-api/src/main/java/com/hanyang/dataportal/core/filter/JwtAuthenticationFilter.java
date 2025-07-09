package com.hanyang.dataportal.core.filter;

import com.hanyang.dataportal.core.component.ApiResponseBuilder;
import com.hanyang.dataportal.core.jwt.component.AuthorizationExtractor;
import com.hanyang.dataportal.core.jwt.component.JwtTokenResolver;
import com.hanyang.dataportal.core.jwt.component.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.hanyang.dataportal.core.response.ResponseMessage.*;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenResolver jwtTokenResolver;
    private final JwtTokenValidator jwtTokenValidator;
    private final ApiResponseBuilder apiResponseBuilder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        final String[] excludes = {
                "/api/user/login",
                "/api/user/signup",
                "/api/user/token",
                "/api/dataset",
                "/api/datasets",
                "/api/notices/list",
        };
        final String path = request.getRequestURI();
        return Arrays.stream(excludes).anyMatch(path::startsWith);
    }

    // 이 필터는 "로그인 유저만" 접근 가능한 리소스 요청시 적용되는 필터임 (로그인 해야할 때만 사용 가능한 api에 적용)
    // -> login, token api의 경우 이 필터를 적용하면 안됨 (= 액세스 토큰이 만료된 경우 요청하는 api기 때문)
    // -> 마찬가지로 dataset list api와 같이 로그인 여부에 상관없는 api도 이 필터를 적용하면 안됨
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Request Header에서 JWT 토큰 추출
        final String token = AuthorizationExtractor.extractAccessToken(request.getHeader("Authorization"));

        // 2. validateToken 으로 토큰 유효성 검사
        if (token != null) {
            if (jwtTokenValidator.validateToken(token)){
                // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장
                Authentication authentication = jwtTokenResolver.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            }
            else{
                // 401 error -> (재)로그인 유도
                apiResponseBuilder.buildErrorResponse((HttpServletResponse) response, UNAUTHORIZED, ACCESS_EXPIRED);
//                apiResponseBuilder.buildErrorResponse((HttpServletResponse) response, BAD_REQUEST, INVALID_JWT);
            }
        }
        else{
            filterChain.doFilter(request, response);
        }
    }
}
