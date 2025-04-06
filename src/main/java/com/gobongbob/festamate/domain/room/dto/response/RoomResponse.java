package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.time.LocalDateTime;
import java.util.List;

public record RoomResponse(
        Long id,
        String title,
        String content,
        String preferredGender,
        LocalDateTime meetingDateTime,
        int maxParticipants,
        List<ParticipantResponse> participants,
        List<ImageResponse> images
) {

    public static RoomResponse fromEntity(Room room, List<RoomParticipant> participants) {
        List<ParticipantResponse> participantResponses = participants.stream()
                .map(participant -> ParticipantResponse.fromEntity(participant, participant.isHost()))
                .toList();
        List<ImageResponse> imageResponses = room.getImages()
                .stream()
                .map(roomImage -> ImageResponse.fromEntity(roomImage.getImage()))
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getTitle(),
                room.getContent(),
                room.getPreferredGender().name(),
                room.getMeetingDateTime(),
                room.getMaxParticipants(),
                participantResponses,
                imageResponses
        );
    }

    private record ParticipantResponse(
            Long id,
            String nickname,
            String studentId,
            String gender,
            String department,
            boolean isHost
    ) {

        private static ParticipantResponse fromEntity(RoomParticipant participant, boolean isHost) {
            return new ParticipantResponse(
                    participant.getId(),
                    participant.getMember().getNickname(),
                    participant.getMember().getStudentId().substring(2, 4),
                    participant.getMember().getGender().name(),
                    participant.getMember().getMajor().getDepartment(),
                    isHost
            );
        }
    }
}
