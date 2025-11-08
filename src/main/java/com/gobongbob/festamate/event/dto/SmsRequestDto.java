package com.gobongbob.festamate.event.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record SmsRequestDto(
        List<String> phoneNumbers,
        String title,
        String openChatUrl
) {

}
