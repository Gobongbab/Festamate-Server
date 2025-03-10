package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.util.List;

public record RoomResponse(
        Long id,
        int headCount,
        String preferredGender,
        String openChatLink,
        String meetingDateTime,
        String title,
        String content,
        List<ParticipantResponse> participants
) {

    public static RoomResponse fromEntity(Room room, List<RoomParticipant> participants) {
        List<ParticipantResponse> participantResponses = participants.stream()
                .map(participant -> ParticipantResponse.fromEntity(participant, participant.isHost()))
                .toList();

        return new RoomResponse(
                room.getId(),
                room.getHeadCount(),
                room.getPreferredGender().name(),
                room.getOpenChatLink(),
                room.getMeetingDateTime().toString(),
                room.getTitle(),
                room.getContent(),
                participantResponses
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
                    participant.getMember().getStudentId(),
                    participant.getMember().getGender().name(),
                    participant.getMember().getMajor().getDepartment(),
                    isHost
            );
        }
    }
}
