package com.gobongbob.festamate.domain.room.dto.response;

import static com.gobongbob.festamate.global.response.ResponseCode.NO_ROOM_IMAGE;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.time.LocalDateTime;

public record RoomListResponse(
        Long id,
        String title,
        Status status,
        String place,
        String content,
        String openChatUrl,
        Gender preferredGender,
        int preferredStudentIdMin,
        int preferredStudentIdMax,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingDateTime,
        int maxParticipants,
        int currentParticipants,
        ImageResponse thumbnail
) {

    public static RoomListResponse fromEntity(Room room) {
        if (room.getImages().isEmpty()) {
            throw new BadRequestException(NO_ROOM_IMAGE);
        }
        Image thumbnail = room.getImages().get(0).getImage();
        return new RoomListResponse(
                room.getId(),
                room.getTitle(),
                room.getStatus(),
                room.getPlace(),
                room.getContent(),
                room.getOpenChatUrl(),
                room.getPreferredGender(),
                room.getPreferredStudentIdMin(),
                room.getPreferredStudentIdMax(),
                room.getMeetingDateTime(),
                room.getMaxParticipants(),
                room.getParticipants().size(),
                ImageResponse.fromEntity(thumbnail)
        );
    }
}
