package com.gobongbob.festamate.domain.chatRoom.repository;

import com.gobongbob.festamate.domain.chatRoom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
