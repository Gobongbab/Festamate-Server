package com.gobongbob.festamate.event.dto;

import lombok.Builder;

@Builder
public record SmsRequestDto(
        String phoneNumber,
        String title,
        String openChatUrl
) {

}
