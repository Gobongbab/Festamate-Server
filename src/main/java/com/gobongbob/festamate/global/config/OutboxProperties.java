package com.gobongbob.festamate.global.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Component
@Configuration
public class OutboxProperties {

    private final int maxAttempts = 5;
    private final int batchSize = 100;
}
