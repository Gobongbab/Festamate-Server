package com.gobongbob.festamate.domain.chat.persistence;

import static com.gobongbob.festamate.domain.chat.domain.QChatRoom.chatRoom;
import static com.gobongbob.festamate.domain.room.domain.QRoom.room;
import static com.gobongbob.festamate.domain.room.domain.QRoomParticipant.roomParticipant;

import com.gobongbob.festamate.domain.chat.domain.ChatRoom;
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
public class ChatRoomQueryDslRepositoryImpl implements ChatRoomQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 모임 상태, 학과, 학번, 성별에 따른 조회
     */
    @Override
    public Slice<ChatRoom> findParticipatingChatRooms(Pageable pageable, Long memberId) {
        int pageSize = pageable.getPageSize();

        JPAQuery<ChatRoom> basicQuery = queryFactory
                .select(chatRoom)
                .from(roomParticipant)
                .join(roomParticipant.room, room)
                .join(room.chatRoom, chatRoom).fetchJoin()
                .where(roomParticipant.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageSize + 1);

        List<ChatRoom> content = addSortingQuery(basicQuery, pageable.getSort().toString());

        return new SliceImpl<>(content, pageable, hasNextPage(content, pageSize));
    }

    /**
     * 마지막 페이지 여부 확인 메소드
     */
    private boolean hasNextPage(List<ChatRoom> content, int pageSize) {
        boolean hasNext = false;
        if (content.size() > pageSize) {
            hasNext = true;
            content.remove(pageSize);

        }
        return hasNext;
    }

    /*
      정렬 관련 쿼리를 추가하기 위한 메소드
     */
    // 마지막 메시지 전송 시간이 얼마나 최신이냐에 따라서 정렬
    private List<ChatRoom> addSortingQuery(JPAQuery<ChatRoom> basicQuery, String sortType) {
        List<ChatRoom> content;

        switch (sortType) {
            case "id":
                content = basicQuery
                        .orderBy(chatRoom.id.desc())
                        .fetch();
                break;

            case "rid":
                content = basicQuery
                        .orderBy(chatRoom.id.asc())
                        .fetch();
                break;

            case "date":
                content = basicQuery
                        .orderBy(chatRoom.lastMessageTime.desc())
                        .fetch();
                break;

            case "rdate":
                content = basicQuery
                        .orderBy(chatRoom.lastMessageTime.asc())
                        .fetch();
                break;

            default:
                content = basicQuery
                        .orderBy(chatRoom.id.desc())
                        .fetch(); // 기본 정렬
                break;
        }

        return content;
    }
}
