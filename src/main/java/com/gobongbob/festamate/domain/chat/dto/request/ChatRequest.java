package com.gobongbob.festamate.domain.chat.dto.request;

public record ChatRequest(
        Long memberId,
        String message
) {

}
