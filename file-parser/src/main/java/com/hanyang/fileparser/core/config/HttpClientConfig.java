package com.hanyang.fileparser.core.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    static final int MAX_CONN_TOTAL = 2;
    static final int MAX_CONN_PER_ROUTE = 20;
    static final int READ_TIMEOUT = 5000;
    static final int CONN_TIMEOUT = 5000;


    @Bean
    public RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(createHttpClientConnectionManager())
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);


        RestTemplate restTemplate = new RestTemplate(factory);


        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            request.getHeaders().set("Accept-Charset", "UTF-8");
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    private HttpClientConnectionManager createHttpClientConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                        .setConnectTimeout(CONN_TIMEOUT, TimeUnit.MILLISECONDS)
                        .build())
                .build();
    }
}