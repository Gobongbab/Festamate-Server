package com.gobongbob.festamate.domain.member.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(MemberCreateRequest request) {
        Member member = request.toEntity();

        return memberRepository.save(member);
    }
}
