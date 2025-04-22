package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.ParticipantRole;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    RoomParticipant save(RoomParticipant roomParticipant);

    // saveAll() 메서드의 경우, JpaRepository 에 기본적으로 정의되어있음.

    Optional<RoomParticipant> findById(Long id);

    void delete(RoomParticipant roomParticipant);

    void deleteByMember_Id(Long memberId);

    @Transactional
    @Modifying
    @Query("delete from RoomParticipant r where r.room.id = ?1")
    void deleteByRoomId(Long id);

    List<RoomParticipant> findByRoom_Id(Long roomId);

    @Query("SELECT r FROM RoomParticipant r WHERE r.room.id = ?1 AND r.participantRole = ?2")
    List<RoomParticipant> findByRoomAndRole(Long id, ParticipantRole participantRole);

    List<RoomParticipant> findByMember_Id(Long memberId);

    Optional<RoomParticipant> findByRoom_IdAndMember_Id(Long roomId, Long memberId);

    int countByRoom_Id(Long roomId);
}
