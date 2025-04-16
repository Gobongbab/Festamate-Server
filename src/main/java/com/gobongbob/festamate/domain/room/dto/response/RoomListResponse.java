package com.gobongbob.festamate.domain.room.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;

public record RoomListResponse(
        Long id,
        String title,
        String place,
        String content,
        String preferredGender,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingDateTime,
        int maxParticipants,
        int currentParticipants,
        ImageResponse thumbnail
) {

    public static RoomListResponse fromEntity(Room room, int currentParticipants) {
        Image thumbnail = room.getImages().get(0).getImage();

        return new RoomListResponse(
                room.getId(),
                room.getTitle(),
                room.getPlace(),
                room.getContent(),
                room.getPreferredGender().getName(),
                room.getMeetingDateTime(),
                room.getMaxParticipants(),
                currentParticipants,
                ImageResponse.fromEntity(thumbnail)
        );
    }
}
