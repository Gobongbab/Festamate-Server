package com.gobongbob.festamate.domain.chat.persistence;

import com.gobongbob.festamate.domain.chat.domain.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.room.id = ?1")
    List<Message> findByRoomId(Long id);
}
