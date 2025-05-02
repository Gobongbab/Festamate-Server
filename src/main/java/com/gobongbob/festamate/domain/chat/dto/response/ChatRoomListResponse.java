package com.gobongbob.festamate.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.chat.domain.ChatRoom;

public record ChatRoomListResponse(
        Long id,
        String name,
        String lastMessageContent,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        String lastMessageTime
) {

    public static ChatRoomListResponse fromEntity(ChatRoom chatRoom) {
        return new ChatRoomListResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getLastMessageContent(),
                chatRoom.getLastMessageTime()
        );
    }
}
