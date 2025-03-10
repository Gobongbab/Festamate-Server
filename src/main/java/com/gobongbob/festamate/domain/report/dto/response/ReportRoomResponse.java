package com.gobongbob.festamate.domain.report.dto.response;

import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.domain.ReportReason;

import java.time.LocalDateTime;

public record ReportRoomResponse(
        Long id,
        Long reporterId,
        String reporterName,
        Long roomId,
        String roomTitle,
        ReportReason reason,
        LocalDateTime reportDate,
        Boolean processed) {

    public static ReportRoomResponse fromEntity(Report report) {
        return new ReportRoomResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getNickname(),
                report.getRoom().getId(),
                report.getRoom().getTitle(),
                report.getReason(),
                report.getReportDate(),
                report.getProcessed()
        );
    }
}