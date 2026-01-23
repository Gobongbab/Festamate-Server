package com.gobongbob.festamate.domain.chat.persistence;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
import com.gobongbob.festamate.domain.room.domain.Room;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomQueryDslRepository {

    Optional<ChatRoom> findByRoom(Room room);

    @Query("""
                select distinct c
                from ChatRoom c
                join c.room r
                join r.participants rp
                where rp.member.id = :memberId
                order by c.lastMessageTime desc nulls last
            """)
    Slice<ChatRoom> findParticipatingChatRooms(Pageable pageable, Long memberId);

    @Transactional
    @Modifying
    @Query("delete from ChatRoom c where c.room.id = ?1")
    void deleteByRoomId(Long id);
}
