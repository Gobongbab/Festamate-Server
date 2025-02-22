package com.gobongbob.festamate.domain.member.domain;

import java.util.Arrays;

public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    public static Gender findByName(String name) {
        return Arrays.stream(Gender.values())
                .filter(gender -> gender.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당하는 성별이 없습니다."));
    }
}
