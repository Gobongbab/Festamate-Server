package com.gobongbob.festamate.domain.report.dto.request;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.room.domain.Room;

import java.time.LocalDateTime;

public record ReportRoomRequest(
        String reason
) {

    public Report toEntity(Member member, Room room) {
        return Report.builder()
                .room(room)
                .reason(reason)
                .reporter(member)
                .reportDate(LocalDateTime.now())
                .processed(false)
                .build();
    }
}