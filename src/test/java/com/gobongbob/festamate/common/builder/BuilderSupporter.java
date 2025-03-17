package com.gobongbob.festamate.common.builder;

import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.persistence.RoomRepository;
import com.gobongbob.festamate.domain.room.presentation.RoomParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuilderSupporter {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomParticipantRepository roomParticipantRepository;

    public MemberRepository memberRepository() {
        return memberRepository;
    }

    public RoomRepository roomRepository() {
        return roomRepository;
    }

    public RoomParticipantRepository roomParticipantRepository() {
        return roomParticipantRepository;
    }
}