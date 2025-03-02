package com.gobongbob.festamate.domain.member.application;

import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.serviceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("MemberServiceTest")
class MemberServiceTest extends serviceSliceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
}
