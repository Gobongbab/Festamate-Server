package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    RoomParticipant save(RoomParticipant roomParticipant);

    // saveAll() 메서드의 경우, JpaRepository 에 기본적으로 정의되어있음.

    Optional<RoomParticipant> findById(Long id);

    void delete(RoomParticipant roomParticipant);

    @Query("""
            SELECT DISTINCT p FROM RoomParticipant p
            JOIN FETCH p.member m
            JOIN FETCH m.profileImage
            WHERE p.room.id = :roomId AND p.participantRole = :participantRole
            """)
    List<RoomParticipant> findByRoomAndRole(Long roomId, ParticipantRole participantRole);

    List<RoomParticipant> findByMember_Id(Long memberId);

    Optional<RoomParticipant> findByRoom_IdAndMember_Id(Long roomId, Long memberId);
}
