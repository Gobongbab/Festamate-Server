package com.gobongbob.festamate.domain.chat.persistence;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ChatRoomQueryDslRepository {

    Slice<ChatRoom> findParticipatingChatRooms(Pageable pageable, Long memberId);
}
