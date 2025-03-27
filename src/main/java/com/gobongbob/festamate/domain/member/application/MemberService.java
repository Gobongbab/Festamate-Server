package com.gobongbob.festamate.domain.member.application;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
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

    public MemberProfileResponse findProfile(Long memberId) { // 로그인한 회원의 pk로 추후 수정 필요
        return memberRepository.findById(memberId)
                .map(MemberProfileResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    @Transactional
    public void updateMemberProfileById(Long memberId, ProfileUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        member.updateProfile(request.nickname(), request.loginPassword());
    }

    @Transactional
    public void deleteMemberById(Long memberId) { // 추후 soft delete 적용이 필요합니다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        memberRepository.delete(member);
    }

    // 프로필 등록 API
    @Transactional
    public void registerProfile(ProfileRegisterRequest request, Long userId) {
        checkNicknameDuplication(request.getNickname()); // 프로필 등록 중 닉네임 중복 확인 필요함

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        Member registeredMember = request.toEntity(member); // 기존 Member 정보 그대로 사용
        memberRepository.save(registeredMember);
    }

    // 닉네임 중복 체크
    @Transactional
    public void checkNicknameDuplication(String nickname) {
        boolean isDuplicate = memberRepository.existsByNickname(nickname);

        if (isDuplicate) {
            throw new IllegalArgumentException("중복된 닉네임입니다.");
        }
    }

    // 아래부터는 oauth2를 위한 메서드
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected member"));
    }

}
