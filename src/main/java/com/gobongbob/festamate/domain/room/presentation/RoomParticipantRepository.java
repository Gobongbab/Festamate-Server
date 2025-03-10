package com.gobongbob.festamate.domain.room.presentation;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.RoomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    RoomParticipant save(RoomParticipant roomParticipant);

    // saveAll() 메서드의 경우, JpaRepository 에 기본적으로 정의되어있음.

    Optional<RoomParticipant> findById(Long id);

    void delete(RoomParticipant roomParticipant);

    void deleteByMember_Id(Long memberId);

    void deleteByRoom(Room room);

    List<RoomParticipant> findByRoom_Id(Long roomId);

    List<RoomParticipant> findByMember_Id(Long memberId);

    Optional<RoomParticipant> findByRoom_IdAndMember_Id(Long roomId, Long memberId);

    int countByRoom_Id(Long roomId);
}
