package com.gobongbob.festamate.domain.member.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import java.util.List;
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

    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    public MemberResponse findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    public MemberProfileResponse findProfile(Member member) {
        return MemberProfileResponse.fromEntity(member);
    }

    @Transactional
    public void updateMemberProfileById(Member member, ProfileUpdateRequest request) {
        member.updateProfile(request.nickname(), request.loginPassword());
    }

    @Transactional
    public void deleteMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        /**
         * 1. 삭제하려는 사용자가 로그인한 사용자와 같은지 확인하는 로직 필요
         * 2. 삭제하려는 사용자가 관리자인지 확인하는 로직 필요
         * 3. 삭제하려는 사용자를 참여중인 방에서 추방시키는 로직 필요
         * 4. 삭제하려는 사용자가 방장이라면 방을 폭파시키는 로직 필요
         */

        memberRepository.delete(member);
    }

    // 아래부터는 oauth2를 위한 메서드
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected member"));
    }

}
