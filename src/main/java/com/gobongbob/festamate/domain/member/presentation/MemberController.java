package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.request.MemberExistRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.room.dto.response.MemberExistResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController implements MemberApi {

    private final MemberService memberService;

    // 모든 회원 조회
    @Override
    @GetMapping("")
    public SuccessResponse<List<MemberResponse>> findAllMembers() {
        return new SuccessResponse<>(memberService.findAllMembers());
    }

    // 회원 상세 조회
    @Override
    @GetMapping("/{memberId}")
    public SuccessResponse<MemberResponse> findMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        return new SuccessResponse<>(memberService.findMemberById(memberId));
    }

    // 프로필 조회
    @Override
    @GetMapping("/profile")
    public SuccessResponse<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(memberService.findProfile(memberDetails.getMember()));
    }

    // 나의 프로필 수정(닉네임)
    @Override
    @PatchMapping("/profile")
    public SuccessResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);
        return new SuccessResponse<>();
    }

    // 나의 프로필 사진 수정
    @Override
    @PutMapping("/profile/photo")
    public SuccessResponse<Void> updateProfilePhoto(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        memberService.updateProfilePhoto(memberDetails.getMember(), profileImage);
        return new SuccessResponse<>();
    }

    // 회원 삭제
    @Override
    @DeleteMapping("/{memberId}")
    public SuccessResponse<Void> deleteMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        memberService.deleteMemberById(memberId);
        return new SuccessResponse<>();
    }

    // 회원 존재 여부 확인
    @Override
    @PostMapping("/exist")
    public SuccessResponse<MemberExistResponse> checkMemberExist(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody MemberExistRequest request
    ) {
        return new SuccessResponse<>(memberService.checkMemberExist(request.phoneNumber()));
    }

    // 닉네임 중복 확인
    @Override
    @GetMapping("/check/nickname")
    public SuccessResponse<String> checkNickname(
            @RequestParam(name = "nickname") String nickname
    ) {
        memberService.checkNicknameDuplication(nickname);
        return new SuccessResponse<>();
    }
}