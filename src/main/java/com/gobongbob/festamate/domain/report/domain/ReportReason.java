package com.gobongbob.festamate.domain.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportReason {
    UNHEALTHY("부적절한 내용"),
    ABUSE("욕설 및 비방"),
    ADVERTISING("광고 및 홍보"),
    SPLASH("도배"),
    POLITICS("정치적 발언"),
    IMPERSONATION("타인 사칭"),
    ILLEGAL("불법 행위");

    private final String name;
}