package com.gobongbob.festamate.domain.room.persistence;

import static com.gobongbob.festamate.domain.room.domain.QRoom.room;
import static org.springframework.util.StringUtils.hasText;

import com.gobongbob.festamate.domain.member.domain.Gender;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import com.gobongbob.festamate.domain.room.domain.Status;
import com.gobongbob.festamate.domain.room.dto.request.FilteringCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
    public Slice<Room> findBySearchCondition(Pageable pageable, FilteringCondition filteringCondition) {
        int pageSize = pageable.getPageSize();

        JPAQuery<Room> basicQuery = queryFactory
                .selectFrom(room)
                .where(
                        statusEquals(filteringCondition.status()),
                        genderEquals(filteringCondition.gender()),
                        participantsEquals(filteringCondition.participants()),
                        studentIdContains(filteringCondition.minStudentId(), filteringCondition.maxStudentId())
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1);

        List<Room> content = addSortingQuery(basicQuery, pageable.getSort().toString());

        return new SliceImpl<>(content, pageable, hasNextPage(content, pageSize));
    }

    @Override
    public Slice<Room> findRecommendedRooms(Pageable pageable, Member member) {
        int pageSize = pageable.getPageSize();

        JPAQuery<Room> basicQuery = queryFactory
                .selectFrom(room)
                .where(
                        genderNotEquals(member.getGender()),
                        studentIdContains(member.getStudentId())
                )
                .offset(pageable.getOffset())
                .limit(pageSize + 1);

        List<Room> content = addSortingQuery(basicQuery, pageable.getSort().toString());

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

    private BooleanExpression statusEquals(Status status) {
        if (status != null && hasText(status.getName())) {
            return room.status.eq(status);
        }

        return null;
    }

    private BooleanExpression participantsEquals(Integer participants) {
        if (participants != null) {
            return room.maxParticipants.eq(participants);
        }

        return null;
    }

    private BooleanExpression genderEquals(Gender gender) {
        if (gender != null && hasText(gender.getName())) {
            return room.preferredGender.eq(gender);
        }

        return null;
    }

    private BooleanExpression genderNotEquals(Gender gender) {
        if (gender != null && hasText(gender.getName())) {
            Gender oppositeGender = gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;

            return room.preferredGender.eq(oppositeGender);
        }

        return null;
    }

    private BooleanExpression studentIdContains(String minStudentId, String maxStudentId) {
        if (hasText(minStudentId) && hasText(maxStudentId)) {
            // 모임방의 최소 학번 조건이 25일 경우 24는 통과하고, 최대 학번 조건이 20일 경우 19는 통과하지 못함
            return room.preferredStudentIdMin.goe(minStudentId).and(room.preferredStudentIdMax.loe(maxStudentId));
        }

        return null;
    }

    private BooleanExpression studentIdContains(String studentId) {
        if (hasText(studentId)) {
            // 모임방의 최소 학번 조건이 25일 경우 24는 통과하고, 최대 학번 조건이 20일 경우 19는 통과하지 못함
            return room.preferredStudentIdMin.goe(studentId).and(room.preferredStudentIdMax.loe(studentId));
        }

        return null;
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
                break;

            case "rdate":
                content = basicQuery
                        .orderBy(room.meetingDateTime.asc())
                        .fetch();
                break;

            default:
                content = basicQuery
                        .orderBy(room.id.desc())
                        .fetch(); // 기본 정렬
                break;
        }

        return content;
    }
}
