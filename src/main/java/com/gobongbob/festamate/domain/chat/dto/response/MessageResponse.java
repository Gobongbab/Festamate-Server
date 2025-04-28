package com.gobongbob.festamate.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gobongbob.festamate.domain.chat.domain.Message;
import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String nickname,
        String message,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime sendDate
) {

    public static MessageResponse fromEntity(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getSendDate()
        );
    }
}
