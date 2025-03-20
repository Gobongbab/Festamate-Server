package com.gobongbob.festamate.domain.chat.persistence;

import com.gobongbob.festamate.domain.chat.domain.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("select c from Chat c where c.room.id = ?1")
    List<Chat> findByRoomId(Long id);
}
