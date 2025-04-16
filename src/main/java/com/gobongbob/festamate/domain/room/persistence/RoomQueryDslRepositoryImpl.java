package com.gobongbob.festamate.domain.room.persistence;

import static com.gobongbob.festamate.domain.room.domain.QRoom.room;
import static com.gobongbob.festamate.domain.room.domain.QRoomParticipant.roomParticipant;
import static org.springframework.util.StringUtils.hasText;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.SearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomQueryDslRepositoryImpl implements RoomQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 모임 상태, 학과, 학번, 성별에 따른 조회
     */
    @Override
    public Slice<Room> findBySearchCondition(Pageable pageable, SearchCondition searchCondition) {
        int pageSize = pageable.getPageSize();

        JPAQuery<Room> basicQuery = queryFactory
                .selectFrom(room)
                .where(
                        statusEquals(searchCondition.status()),
                        participantsEquals(searchCondition.participants()),
                        studentIdContains(searchCondition.studentId()),
                        genderEquals(searchCondition.gender())
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1);

        List<Room> content = addSortingQuery(basicQuery, searchCondition.sortType());

        return new SliceImpl<>(content, pageable, hasNextPage(content, pageSize));
    }

    /**
     * 마지막 페이지 여부 확인 메소드
     */
    private boolean hasNextPage(List<Room> content, int pageSize) {
        boolean hasNext = false;
        if (content.size() > pageSize) {
            hasNext = true;
            content.remove(pageSize);

        }
        return hasNext;
    }

    private BooleanExpression participantsEquals(int participants) {
        return hasText(String.valueOf(participants)) ? room.maxParticipants.eq(participants) : null;
    }

    // 방 안에 입력받은 학번을 가진 사람이 있는지 확인
    private BooleanExpression studentIdContains(String studentId) {
        JPQLQuery<Long> roomIdsOfStudentIdMatched = queryFactory
                .select(roomParticipant.room.id)
                .from(roomParticipant)
                .where(roomParticipant.member.studentId.startsWith(studentId));

        return room.id.in(roomIdsOfStudentIdMatched);
    }

    private BooleanExpression genderEquals(String gender) {
        return hasText(gender) ? room.preferredGender.eq(Gender.valueOf(gender)) : null;
    }

    private BooleanExpression statusEquals(String status) {
        if (!hasText(status)) {
            return null;
        }

        return room.status.eq(Status.valueOf(status));
    }

    /*
      정렬 관련 쿼리를 추가하기 위한 메소드
     */
    private List<Room> addSortingQuery(JPAQuery<Room> basicQuery, String sortType) {
        List<Room> content;

        switch (sortType) {
            case "id":
                content = basicQuery
                        .orderBy(room.id.desc())
                        .fetch();
                break;

            case "rid":
                content = basicQuery
                        .orderBy(room.id.asc())
                        .fetch();
                break;

            case "date":
                content = basicQuery
                        .orderBy(room.meetingDateTime.desc())
                        .fetch();

            case "rdate":
                content = basicQuery
                        .orderBy(room.meetingDateTime.asc())
                        .fetch();

            default:
                content = new ArrayList<>();
        }

        return content;
    }
}
