package com.gobongbob.festamate.domain.chat.dto.response;

import com.gobongbob.festamate.domain.chat.domain.Message;
import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String nickname,
        String message,
        LocalDateTime sendDate
) {

    public static MessageResponse fromEntity(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getNickname(),
                message.getMessage(),
                message.getSendDate()
        );
    }
}
