package com.gobongbob.festamate.domain.room.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.ROLE_NOT_FOUND;

import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.Arrays;

public enum Role {
    HOST("HOST"),
    GUEST("GUEST");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(ROLE_NOT_FOUND));
    }
}
