package com.gobongbob.festamate.domain.room.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ParticipantRole {
    HOST("호스트"),
    GUEST("게스트");

    private final String name;
}
