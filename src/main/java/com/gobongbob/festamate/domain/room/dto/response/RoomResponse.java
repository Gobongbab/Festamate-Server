package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.room.domain.Room;

public record RoomResponse(
        Long id,
        int headCount,
        String preferredGender,
        String openChatLink,
        String meetingDateTime,
        String title,
        String content,
        HostResponse host
) {

    public static RoomResponse toDto(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHeadCount(),
                room.getPreferredGender().name(),
                room.getOpenChatLink(),
                room.getMeetingDateTime().toString(),
                room.getTitle(),
                room.getContent(),
                new HostResponse(
                        room.getHost().getId(),
                        room.getHost().getNickname(),
                        room.getHost().getStudentId(),
                        room.getHost().getGender().name(),
                        room.getHost().getMajor().getDepartment()
                )
        );
    }

    public static record HostResponse(
            Long id,
            String nickname,
            String studentId,
            String gender,
            String department
    ) {

    }
}
