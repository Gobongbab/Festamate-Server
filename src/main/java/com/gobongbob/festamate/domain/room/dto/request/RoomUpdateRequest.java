package com.gobongbob.festamate.domain.room.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;

public record RoomUpdateRequest(
        int headCount,
        String preferredGender,
        String openChatLink,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingDateTime,
        String title,
        String content
) {

    public Room toEntity() {
        return Room.builder()
                .headCount(headCount)
                .preferredGender(Gender.findByName(preferredGender))
                .openChatLink(openChatLink)
                .meetingDateTime(meetingDateTime)
                .title(title)
                .content(content)
                .build();
    }
}
