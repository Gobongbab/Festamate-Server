package com.gobongbob.festamate.domain.room.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RoomCreateRequest(
        @Size(max = 20, message = "제목은 최대 20자까지 입력 가능합니다") String title,
        @Size(max = 200, message = "글 내용은 최대 200자까지 입력 가능합니다") String content,
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
