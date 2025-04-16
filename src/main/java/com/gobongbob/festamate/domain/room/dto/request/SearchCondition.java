package com.gobongbob.festamate.domain.room.dto.request;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Status;

public record SearchCondition(
        Status status,
        Gender gender,
        Integer participants,
        String studentId
) {

}
