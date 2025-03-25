package com.gobongbob.festamate.domain.chat.persistence;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
