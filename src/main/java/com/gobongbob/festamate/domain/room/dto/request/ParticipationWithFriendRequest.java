package com.gobongbob.festamate.domain.room.dto.request;

import java.util.List;

public record ParticipationWithFriendRequest(
        List<String> friendPhoneNumbers
) {

}
