package com.gobongbob.festamate.domain.room.dto.request;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Status;

public record FilteringCondition(
        String keyword,
        Status status,
        Gender gender,
        String minStudentId,
        String maxStudentId,
        Integer participants
) {

}
