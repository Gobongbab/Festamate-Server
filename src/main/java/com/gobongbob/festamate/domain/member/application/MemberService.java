package com.gobongbob.festamate.domain.member.application;

import static com.gobongbob.festamate.global.response.ResponseCode.DUPLICATE_NICKNAME;
import static com.gobongbob.festamate.global.response.ResponseCode.DUPLICATE_STUDENT_ID;
import static com.gobongbob.festamate.global.response.ResponseCode.ERROR_SEND_SMS;
import static com.gobongbob.festamate.global.response.ResponseCode.FAIL_SEND_SMS;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_ADMIN;
import static com.gobongbob.festamate.global.response.ResponseCode.NO_MEMBER;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.domain.ProfileImage;
import com.gobongbob.festamate.domain.image.infrastructure.ImageService;
import com.gobongbob.festamate.domain.image.persistence.ProfileImageRepository;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Role;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.domain.report.domain.Report;
import com.gobongbob.festamate.domain.report.persistence.ReportRepository;
import com.gobongbob.festamate.domain.room.dto.response.MemberExistResponse;
import com.gobongbob.festamate.global.aop.CheckActiveUser;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private static final String DEFAULT_PROFILE_IMAGE_NAME = "default_profile_image.png";

    private final MemberRepository memberRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ImageService imageService;
    private final ReportRepository reportRepository;
    private final DefaultMessageService messageService;

    @Value("${coolsms.from.number}")
    private String fromNumber;

    @Transactional
    public Member createMember(MemberCreateRequest request) {
        Member member = request.toEntity();
        profileImageRepository.findByStoreName(DEFAULT_PROFILE_IMAGE_NAME)
                .ifPresent(member::initializeProfileImage);

        return memberRepository.save(member);
    }

    // 모든 회원 조회
    @CheckActiveUser
    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAllWithProfileImage()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    // 회원 상세 조회
    @CheckActiveUser
    public MemberResponse findMemberById(Long memberId) {
        return memberRepository.findByIdWithProfileImage(memberId)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

    // 관리자용 유저 조회
    public MemberResponse findMemberByIdForAdmin(CustomMemberDetails memberDetails, Long memberId) {
        Role requesterRole = memberDetails.getMember().getRole();

        if (requesterRole != Role.ADMIN) {
            throw new BadRequestException(NO_ADMIN);
        }

        return memberRepository.findByIdWithProfileImage(memberId)
                .map(MemberResponse::fromEntity)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

    // 관리자용 사용자 이름 검색 (목록 반환)
    public List<MemberResponse> findMembersByNameForAdmin(CustomMemberDetails memberDetails,
            String memberName) {
        Role requesterRole = memberDetails.getMember().getRole();

        if (requesterRole != Role.ADMIN) {
            throw new BadRequestException(NO_ADMIN);
        }

        List<Member> foundMembers = memberRepository.findAllByNameWithProfileImage(memberName);

        return foundMembers.stream()
                .map(MemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Member findMembersById(Long memberId) {
        return memberRepository.findByIdWithProfileImage(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

    public MemberProfileResponse findProfile(Member member) {
        return MemberProfileResponse.fromEntity(member);
    }

    // 나의 프로필 수정(닉네임)
    @Transactional
    @CheckActiveUser
    public void updateMemberProfileById(Member member, ProfileUpdateRequest request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new BadRequestException(DUPLICATE_NICKNAME);
        }
        member.updateProfile(request.nickname());
        memberRepository.save(member);
    }

    // 나의 프로필 사진 수정
    @Transactional
    @CheckActiveUser
    public void updateProfilePhoto(Member member, MultipartFile profileImageFile) {
        // 1. 기존 프로필 이미지 엔티티 가져오기
        ProfileImage currentProfileImageWrapper = member.getProfileImage();

        Image currentProfileImage = null;
        if (currentProfileImageWrapper != null) {
            currentProfileImage = currentProfileImageWrapper.getImage();
        }

        // 2. 새로운 프로필 이미지 업로드
        Image newImage = imageService.uploadImage(profileImageFile);

        // 3. Member 엔티티의 프로필 이미지 참조 업데이트
        ProfileImage newProfileImageWrapper = ProfileImage.builder()
                .image(newImage)
                .build();
        profileImageRepository.save(newProfileImageWrapper);
        member.initializeProfileImage(newProfileImageWrapper);
        memberRepository.save(member);

        // 4. 기존 프로필 이미지가 있었다면 삭제
        if (currentProfileImage != null) {
            imageService.delete(currentProfileImage);
        }
    }

    // 회원 삭제
    @Transactional
    @CheckActiveUser
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
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
        member.block();
        memberRepository.save(member);
        sendMatchingCompleteMessages(member);

        // 해당 유저가 피신고자인 모든 신고의 processed = true 처리
        List<Report> reports = reportRepository.findByReportedMember(member);
        for (Report report : reports) {
            report.setProcessed(true);
        }
        reportRepository.saveAll(reports);
    }

    // 유저 제재 해제(admin)
    @Transactional
    public void unblockMemberById(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
        member.unblock();
        memberRepository.save(member);
    }

    // 닉네임 중복 체크
    public void checkNicknameDuplication(String nickname) {
        boolean isDuplicate = memberRepository.existsByNickname(nickname);

        if (isDuplicate) {
            throw new BadRequestException(DUPLICATE_NICKNAME);
        }
    }

    // 학번 중복 체크
    public void checkStudentIdDuplication(String studentId) {
        boolean isDuplicate = memberRepository.existsByStudentId(studentId);

        if (isDuplicate) {
            throw new BadRequestException(DUPLICATE_STUDENT_ID);
        }
    }

    // 회원 존재 여부 확인
    public MemberExistResponse checkMemberExist(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));

        return new MemberExistResponse(memberRepository.existsByPhoneNumber(phoneNumber), member.getGender());
    }

    // 아래부터는 oauth2를 위한 메서드
    public Member findById(Long memberId) {
        return memberRepository.findByIdWithProfileImage(memberId)
                .orElseThrow(() -> new BadRequestException(NO_MEMBER));
    }

    private void sendMatchingCompleteMessages(Member member) {
        Message message = setMessage(member.getPhoneNumber());
        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException e) {
            log.error("(NurigoMessageNotReceivedException) 휴대폰 문자 전송 에러 상세 내용: " + e);
            throw new BadRequestException(FAIL_SEND_SMS);
        } catch (Exception e) {
            log.error("(Exception) 휴대폰 문자 전송 에러 상세 내용: " + e);
            throw new BadRequestException(ERROR_SEND_SMS);
        }
    }

    private Message setMessage(String phoneNumber) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phoneNumber);
        message.setText("[FestaMate!] 부적절한 행위로 인해 제재되었어요. 자세한 사항은 관리자를 통해 문의해주세요.");

        return message;
    }
}
