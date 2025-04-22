package com.gobongbob.festamate.domain.room.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomAuthority;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import com.gobongbob.festamate.domain.room.domain.Status;
import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
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
        RoomAuthority roomAuthority,
        List<ParticipantResponse> hostParticipants,
        List<ParticipantResponse> guestParticipants,
        List<ImageResponse> images
) {

    public static RoomResponse fromEntity(
            Room room,
            RoomAuthority roomAuthority,
            List<RoomParticipant> hostParticipants,
            List<RoomParticipant> guestParticipants
    ) {
        return new RoomResponse(
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
                roomAuthority,
                toParticipantResponse(hostParticipants),
                toParticipantResponse(guestParticipants),
                toImageResponse(room)
        );
    }

    private static List<ParticipantResponse> toParticipantResponse(List<RoomParticipant> participants) {
        return participants.stream()
                .map(participant -> ParticipantResponse.fromEntity(participant, participant.isHost()))
                .toList();
    }

    private static List<ImageResponse> toImageResponse(Room room) {
        return room.getImages()
                .stream()
                .map(roomImage -> ImageResponse.fromEntity(roomImage.getImage()))
                .toList();
    }

    private record ParticipantResponse(
            Long id,
            String nickname,
            String studentId,
            String gender,
            String major,
            boolean isHost
    ) {

        private static ParticipantResponse fromEntity(RoomParticipant participant, boolean isHost) {
            return new ParticipantResponse(
                    participant.getMember().getId(),
                    participant.getMember().getNickname(),
                    participant.getMember().getStudentId().substring(2, 4),
                    participant.getMember().getGender().name(),
                    participant.getMember().getStudentDepartment(),
                    isHost
            );
        }
    }
}
