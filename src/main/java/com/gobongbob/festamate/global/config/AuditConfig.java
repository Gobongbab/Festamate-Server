package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            CustomMemberDetails memberDetails = (CustomMemberDetails) authentication.getPrincipal();
            return Optional.of(memberDetails.getMember().getName());
        };
    }
}
