package com.gobongbob.festamate.domain.room.persistence;

import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.dto.request.SearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface RoomQueryDslRepository {

    Slice<Room> findBySearchCondition(Pageable pageable, SearchCondition searchCondition);
}
