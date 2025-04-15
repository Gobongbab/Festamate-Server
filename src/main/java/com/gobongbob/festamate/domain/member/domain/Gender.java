package com.gobongbob.festamate.domain.member.domain;

import static com.gobongbob.festamate.global.response.ResponseCode.NO_GENDER;

import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    ANY("무관");

    private final String name;

    public static Gender findByName(String name) {
        return Arrays.stream(Gender.values())
                .filter(gender -> gender.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(NO_GENDER));
    }
}
