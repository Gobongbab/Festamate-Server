package com.gobongbob.festamate.domain.room.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoomAuthority {
    HOST("호스트"),
    PARTICIPANT("참여 회원"),
    NON_PARTICIPANT("미참여 회원"),
    NON_MEMBER("비회원");

    private final String name;
}
