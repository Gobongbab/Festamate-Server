package com.gobongbob.festamate.domain.report.dto.request;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.domain.ReportReason;

import java.time.LocalDateTime;

public record ReportMemberRequest(
        String reason
) {

    public Report toEntity(Member reporter, Member reportedMember) {
        return Report.builder()
                .reason(ReportReason.findByName(reason))
                .reporter(reporter)
                .reportedMember(reportedMember)
                .reportDate(LocalDateTime.now())
                .processed(false)
                .build();
    }
}