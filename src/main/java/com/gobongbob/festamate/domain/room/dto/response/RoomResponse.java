package com.gobongbob.festamate.domain.room.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.image.dto.response.ImageResponse;
import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
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
        int preferredStudentIdMin,
        int preferredStudentIdMax,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingDateTime,
        int maxParticipants,
        int currentParticipants,
        RoomAuthority roomAuthority,
        List<ParticipantResponse> hostParticipants,
        List<ParticipantResponse> guestParticipants,
        ImageResponse thumbnail
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
                room.getParticipants().size(),
                roomAuthority,
                toParticipantResponse(hostParticipants),
                toParticipantResponse(guestParticipants),
                ImageResponse.fromEntity(room.getImages().get(0).getImage())
        );
    }

    private static List<ParticipantResponse> toParticipantResponse(List<RoomParticipant> participants) {
        return participants.stream()
                .map(ParticipantResponse::fromEntity)
                .toList();
    }


    private record ParticipantResponse(
            Long id,
            String nickname,
            String studentId,
            String gender,
            String department,
            boolean isHost,
            String profileImageUrl
    ) {

        private static ParticipantResponse fromEntity(RoomParticipant participant) {
            Member member = participant.getMember();

            return new ParticipantResponse(
                    member.getId(),
                    member.getNickname(),
                    member.getStudentId().substring(2, 4),
                    member.getGender().name(),
                    member.getStudentDepartment(),
                    participant.isHost(),
                    member.getProfileImage().getUrl()
            );
        }
    }
}
