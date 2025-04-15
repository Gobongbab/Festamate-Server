package com.gobongbob.festamate.domain.room.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.ROLE_NOT_FOUND;

import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    MATCHING("매칭중"),
    MATCHED("매칭 완료"),
    CLOSED("모임 종료");

    private final String name;

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(ROLE_NOT_FOUND));
    }
}
