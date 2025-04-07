package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.time.LocalDateTime;

public record RoomListResponse(
        Long id,
        String title,
        String content,
        String preferredGender,
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
                room.getContent(),
                room.getPreferredGender().name(),
                room.getMeetingDateTime(),
                room.getMaxParticipants(),
                currentParticipants,
                ImageResponse.fromEntity(thumbnail)
        );
    }
}
