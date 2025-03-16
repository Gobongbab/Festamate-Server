package com.gobongbob.festamate.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberNicknameResponse {

    private String nickname; // 카카오 서버로부터 받아온 닉네임
}