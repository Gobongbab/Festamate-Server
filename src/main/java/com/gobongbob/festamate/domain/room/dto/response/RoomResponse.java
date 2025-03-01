package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.room.domain.Room;

public record RoomResponse(
        Long id,
        int headCount,
        String preferredGender,
        String openChatLink,
        String meetingDateTime,
        String title,
        String content
) {
    public static RoomResponse toDto(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHeadCount(),
                room.getPreferredGender().name(),
                room.getOpenChatLink(),
                room.getMeetingDateTime().toString(),
                room.getTitle(),
                room.getContent()
        );
    }
}
