package com.gobongbob.festamate.common.builder;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.room.domain.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestFixtureBuilder {

    @Autowired
    private BuilderSupporter bs;

    public Member buildMember(Member member) {
        return bs.memberRepository().save(member);
    }

    public void deleteMember(Member member) {
        bs.memberRepository().delete(member);
    }

    public Room buildRoom(Room room) {
        return bs.roomRepository().save(room);
    }

    public void deleteRoom(Room room) {
        bs.roomRepository().delete(room);
    }
}
