package com.hanyang.datastore.config;

import com.hanyang.datastore.core.filter.JwtAuthenticationFilter;
import com.hanyang.datastore.core.jwt.JwtTokenValidator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private JwtTokenValidator jwtTokenValidator;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
}
