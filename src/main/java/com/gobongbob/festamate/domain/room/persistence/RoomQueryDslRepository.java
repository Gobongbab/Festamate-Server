package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RoomQueryDslRepository {

    Slice<Room> findBySearchCondition(Pageable pageable, FilteringCondition filteringCondition);

    Slice<Room> findRecommendedRooms(Pageable pageable, Member member);
}
