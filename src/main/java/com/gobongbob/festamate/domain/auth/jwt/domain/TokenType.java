package com.gobongbob.festamate.domain.auth.jwt.domain;

import java.time.Duration;

public enum TokenType {
    FINAL_ACCESS("final_access", Duration.ofHours(2), false, false), // 2시간
    FINAL_REFRESH("final_refresh", Duration.ofDays(180), false, false), // 6개월(180일)
    ADMIN_ACCESS("admin_access", Duration.ofDays(100), true, false),
    ADMIN_REFRESH("admin_refresh", Duration.ofDays(100), true, false),
    TEST_ACCESS("test_access", Duration.ofDays(100), false, true),
    TEST_REFRESH("test_refresh", Duration.ofDays(100), false, true); // 100일

    private final String type;
    private final Duration duration;
    private final boolean isAdmin;
    private final boolean isTest;

    TokenType(String type, Duration duration, boolean isAdmin, boolean isTest) {
        this.type = type;
        this.duration = duration;
        this.isAdmin = isAdmin;
        this.isTest = isTest;
    }

    public String getType() {
        return type;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isTest() {
        return isTest;
    }
}
