package com.gobongbob.festamate.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.domain.ReportReason;
import java.time.LocalDateTime;

/*
reporter가 신고 하는 사람
reportee가 신고 당하는 사람
 */
public record ReportRoomResponse(
        Long id,
        Long reporterId, // 신고한 사람 ID
        String reporterNickname, // 신고한 사람 이름
        Long reporteeId, // 신고 당한 사람 ID
        String reporteeNickname, // 신고 당한 사람 이름
        Long roomId,
        String roomTitle,
        ReportReason reason,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime reportDate,
        Boolean processed) {

    public static ReportRoomResponse fromEntity(Report report) {
        return new ReportRoomResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getNickname(),
                report.getReportedMember() != null ? report.getReportedMember().getId() : null,
                report.getReportedMember() != null ? report.getReportedMember().getNickname() : null,
                report.getRoom() != null ? report.getRoom().getId() : null,
                report.getRoom() != null ? report.getRoom().getTitle() : null,
                report.getReason(),
                report.getReportDate(),
                report.getProcessed()
        );
    }
}