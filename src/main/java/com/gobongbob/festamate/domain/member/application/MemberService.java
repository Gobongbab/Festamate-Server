package com.gobongbob.festamate.domain.member.application;

import static com.gobongbob.festamate.global.response.ResponseCode.DUPLICATE_NICKNAME;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.room.dto.response.MemberExistResponse;
import com.gobongbob.festamate.domain.sms.application.TokyoSnsService;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final TokyoSnsService tokyoSnsService;

    @Transactional
    public Member createMember(MemberCreateRequest request) {
        Member member = request.toEntity();
        profileImageRepository.findByStoreName("default_profile_image.png")
                .ifPresent(member::initializeProfileImage);

        return memberRepository.save(member);
    }

    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    // 유저 조회
    public MemberResponse findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

    // 관리자용 유저 조회
    public MemberResponse findMemberByIdForAdmin(CustomMemberDetails memberDetails, Long memberId) {
        String role = memberDetails.getMember().getRole();

        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }

        return memberRepository.findById(memberId)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
    }

    public Member findMembersById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
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
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        /**
         * 1. 삭제하려는 사용자가 로그인한 사용자와 같은지 확인하는 로직 필요
         * 2. 삭제하려는 사용자가 관리자인지 확인하는 로직 필요
         * 3. 삭제하려는 사용자를 참여중인 방에서 추방시키는 로직 필요
         * 4. 삭제하려는 사용자가 방장이라면 방을 폭파시키는 로직 필요
         */

        memberRepository.delete(member);
    }


    // 유저 제재(admin)
    @Transactional
    public void blockMemberById(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        member.block();
        memberRepository.save(member);
    }

    // 유저 제재 해제(admin)
    @Transactional
    public void unblockMemberById(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        member.unblock();
        memberRepository.save(member);
    }

    // 프로필 등록 API
    @Transactional
    public void registerProfile(ProfileRegisterRequest request, Long userId) {

        // 전화번호 인증 여부 확인
        String phoneNumber = request.getPhoneNumber();
        if (!tokyoSnsService.isPhoneNumberVerified(phoneNumber)) {
            throw new IllegalArgumentException("전화번호 인증이 완료되지 않았습니다.");
        }

        // 회원 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        // 프로필 정보 업데이트
        Member updatedMember = request.toEntity(member);
        memberRepository.save(updatedMember);

        // 인증 기록 삭제 (더 이상 인증 재사용 안되게)
        tokyoSnsService.removeVerificationInfo(phoneNumber);
    }

    // 닉네임 중복 체크
    @Transactional
    public void checkNicknameDuplication(String nickname) {
        boolean isDuplicate = memberRepository.existsByNickname(nickname);

        if (isDuplicate) {
            throw new BadRequestException(DUPLICATE_NICKNAME);
        }
    }

    public MemberExistResponse checkMemberExist(String phoneNumber) {
        return new MemberExistResponse(memberRepository.existsByPhoneNumber(phoneNumber));
    }

    // 아래부터는 oauth2를 위한 메서드
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

}
