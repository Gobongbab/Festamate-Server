package com.gobongbob.festamate.domain.room.dto.response;

import com.gobongbob.festamate.domain.member.domain.Gender;

public record MemberExistResponse(
        boolean exist,
        Gender gender
) {

}
