package com.gobongbob.festamate.domain.room.dto.request;

public record SearchCondition(
        String status,
        int participants,
        String studentId,
        String gender,
        String sortType
) {

}
