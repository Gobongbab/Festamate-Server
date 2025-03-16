package com.gobongbob.festamate.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberNicknameRequest {

    private String nickname; // 카카오 서버로부터 받아온 닉네임
}
