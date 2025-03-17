package com.gobongbob.festamate.domain.member.dto.request;

public record ProfileUpdateRequest(
        String nickname,
        String loginPassword
) {

}
