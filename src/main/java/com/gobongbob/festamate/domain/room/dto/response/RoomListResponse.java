package com.gobongbob.festamate.domain.room.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import java.time.LocalDateTime;

public record RoomListResponse(
        Long id,
        String title,
        Status status,
        String place,
        String content,
        Gender preferredGender,
        String preferredStudentIdMin,
        String preferredStudentIdMax,
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
                room.getStatus(),
                room.getPlace(),
                room.getContent(),
                room.getPreferredGender(),
                room.getPreferredStudentIdMin(),
                room.getPreferredStudentIdMax(),
                room.getMeetingDateTime(),
                room.getMaxParticipants(),
                currentParticipants,
                ImageResponse.fromEntity(thumbnail)
        );
    }
}
