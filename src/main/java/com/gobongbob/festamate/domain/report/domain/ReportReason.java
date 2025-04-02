package com.gobongbob.festamate.domain.report.domain;

public enum ReportReason {
    UNHEALTHY("음란물/불건전한 만남 및 대화"),
    ABUSE("욕설/비하"),
    ADVERTISING("상업적 광고 및 판매"),
    SPLASH("낚시/놀람/도배"),
    POLITICS("정당/정치인 비하 및 선거운동"),
    IMPERSONATION("유출/사칭/사기"),
    ILLEGAL("불법촬영물 등의 유통");

    private final String name;

    ReportReason(String name) {
        this.name = name;
    }

    public static ReportReason findByName(String name) {
        for (ReportReason reason : ReportReason.values()) {
            if (reason.name.equals(name)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("해당하는 신고 사유가 없습니다.");
    }
}