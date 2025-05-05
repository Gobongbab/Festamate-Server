package com.gobongbob.festamate.domain.report.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.NO_REPORT_REASON;

import com.gobongbob.festamate.global.response.exception.BadRequestException;
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

    public static ReportReason findByName(String name) {
        for (ReportReason reason : ReportReason.values()) {
            if (reason.name.equals(name)) {
                return reason;
            }
        }
        throw new BadRequestException(NO_REPORT_REASON);
    }
}