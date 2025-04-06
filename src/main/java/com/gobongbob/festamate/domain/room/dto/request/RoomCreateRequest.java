package com.gobongbob.festamate.domain.room.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;

public record RoomCreateRequest(
        String title,
        String content,
        String preferredGender,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingDateTime,
        int maxParticipants
) {

    public Room toEntity(Member member) {
        return Room.builder()
                .title(title)
                .content(content)
                .preferredGender(Gender.findByName(preferredGender))
                .meetingDateTime(meetingDateTime)
                .maxParticipants(maxParticipants)
                .host(member)
                .build();
    }
}
