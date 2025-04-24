package com.gobongbob.festamate.domain.room.dto.request;

import java.util.List;

public record FriendPhoneNumbersRequest(
        List<String> friendPhoneNumbers
) {

}
