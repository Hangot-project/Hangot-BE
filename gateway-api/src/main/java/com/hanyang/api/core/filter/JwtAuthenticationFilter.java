package com.hanyang.api.core.filter;

import com.hanyang.api.core.component.ApiResponseBuilder;
import com.hanyang.api.core.jwt.component.AuthorizationExtractor;
import com.hanyang.api.core.jwt.component.JwtTokenResolver;
import com.hanyang.api.core.jwt.component.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.hanyang.api.core.response.ResponseMessage.ACCESS_EXPIRED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenResolver jwtTokenResolver;
    private final JwtTokenValidator jwtTokenValidator;
    private final ApiResponseBuilder apiResponseBuilder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String[] excludes = {
                "/api/user/login/*",
                "/api/user/token",
                "/api/dataset",
                "/api/datasets",
        };

        final String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return Arrays.stream(excludes)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

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
