package com.gobongbob.festamate.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor // 각 Enum 상수에 대한 생성자 자동 생성
public enum Role {
    ROLE_USER("ROLE_USER"), // 일반 사용자 (Spring Security의 기본 Prefix "ROLE_")
    ROLE_ADMIN("ROLE_ADMIN"); // 관리자

    private final String value; // 권한 값 (Spring Security에서 사용될 실제 문자열)

    // Spring Security에서 권한 정보를 인식할 때 사용
    public String getAuthority() {
        return value;
    }
}
