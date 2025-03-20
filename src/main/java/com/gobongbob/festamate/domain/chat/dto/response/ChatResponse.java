package com.gobongbob.festamate.domain.chat.dto.response;

import com.gobongbob.festamate.domain.chat.domain.Chat;
import java.time.LocalDateTime;

public record ChatResponse(
        Long id,
        String nickname,
        String message,
        LocalDateTime sendDate
) {

    public static ChatResponse fromEntity(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getNickname(),
                chat.getMessage(),
                chat.getSendDate()
        );
    }
}
