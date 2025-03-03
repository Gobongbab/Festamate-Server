package com.gobongbob.festamate.common.builder;

import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuilderSupporter {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    public MemberRepository memberRepository() {
        return memberRepository;
    }

    public RoomRepository roomRepository() {
        return roomRepository;
    }
}