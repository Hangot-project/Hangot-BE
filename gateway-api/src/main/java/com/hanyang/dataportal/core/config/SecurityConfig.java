package com.hanyang.dataportal.core.config;

import com.hanyang.dataportal.core.component.CustomAuthenticationEntryPoint;
import com.hanyang.dataportal.core.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig{

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    // security 6.1 최신버전으로 문법을 조금 다르게 사용해야함.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource))

                //폼 로그인 안함
                .formLogin(AbstractHttpConfigurer::disable)

                //세션 안씀
                .sessionManagement((httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)))

                //핸들러
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(customAuthenticationEntryPoint))

                .authorizeHttpRequests((authorizeRequests) -> {
//                    authorizeRequests.requestMatchers(HttpMethod.POST,"/api/dataset/**").hasRole("ADMIN");;
//                    authorizeRequests.requestMatchers(HttpMethod.PUT,"/api/dataset/**").hasRole("ADMIN");
//                    authorizeRequests.requestMatchers(HttpMethod.DELETE,"/api/dataset/**").hasRole("ADMIN");
                    authorizeRequests.requestMatchers(HttpMethod.GET, "api/scrap/**").hasRole("USER");
                    authorizeRequests.requestMatchers(HttpMethod.POST, "/api/scrap/dataset/**").hasRole("USER");
                    authorizeRequests.requestMatchers(HttpMethod.DELETE, "/api/scrap/dataset/**").hasRole("USER");
                    authorizeRequests.anyRequest().permitAll(); // 그 외의 요청은 다 허용
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder myPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}