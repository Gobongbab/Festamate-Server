package com.gobongbob.festamate.domain.room.dto.request;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

public record SearchCondition(
        @Schema(description = "매칭 상태", enumAsRef = true)
        Status status,
        @Schema(description = "입장 가능 성별", enumAsRef = true)
        Gender gender,
        Integer participants,
        String studentId
) {

}
