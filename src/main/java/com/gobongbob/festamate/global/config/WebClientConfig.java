package com.gobongbob.festamate.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${KAKAO_USER_INFO_URI}")
    private String kakaoUserInfoUri;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(kakaoUserInfoUri) // 이걸 기반으로 user info 요청 등 수행
                .build();
    }
}
