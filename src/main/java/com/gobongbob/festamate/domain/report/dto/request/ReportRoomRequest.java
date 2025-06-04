package com.gobongbob.festamate.domain.report.dto.request;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.domain.ReportReason;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;

public record ReportRoomRequest(
        ReportReason reason
) {

    public Report toEntity(Member reporter, Room room, Member reportedMember) {
        return Report.builder()
                .room(room)
                .reason(reason)
                .reporter(reporter)
                .reportedMember(reportedMember)
                .reportDate(LocalDateTime.now())
                .processed(false)
                .build();
    }
}