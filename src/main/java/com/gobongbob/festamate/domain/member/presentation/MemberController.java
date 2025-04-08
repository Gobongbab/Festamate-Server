package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.MemberExistRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.room.dto.response.MemberExistResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenService tokenService;

    @PostMapping("/auth/signup")
    public SuccessResponse<Void> signUp(@RequestBody @Valid MemberCreateRequest request) {
        Member member = memberService.createMember(request);
        return new SuccessResponse<>();
    }

    @GetMapping("/members")
    public SuccessResponse<List<MemberResponse>> findAllMembers() {
        return new SuccessResponse<>(memberService.findAllMembers());
    }

    @GetMapping("/members/{memberId}")
    public SuccessResponse<MemberResponse> findMemberById(@PathVariable Long memberId) {
        return new SuccessResponse<>(memberService.findMemberById(memberId));
    }

    // 프로필 조회
    @GetMapping("/api/auth/members/profile")
    public SuccessResponse<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(memberService.findProfile(memberDetails.getMember()));
    }

    @PatchMapping("/members/profile")
    public SuccessResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);

        return new SuccessResponse<>();
    }

    @DeleteMapping("/members/{memberId}")
    public SuccessResponse<Void> deleteMemberById(@PathVariable Long memberId) {
        memberService.deleteMemberById(memberId);

        return new SuccessResponse<>();
    }

    // 프로필 등록 API
    @PostMapping("/api/auth/register/profile") // 추후 /api/auth를 상위 경로에 작성하도록 변경 필요
    public SuccessResponse<Map<String, String>> registerProfile(
            @RequestBody ProfileRegisterRequest request,

            @AuthenticationPrincipal CustomMemberDetails memberDetails) { // 최소 JWT 정보
        Long userId = memberDetails.getMember().getId();
        memberService.registerProfile(request, userId);

        // TokenService를 이용하여 최종 JWT(access, refresh) 생성
        Map<String, String> tokens = tokenService.generateTokens(userId);

        // 최종 JWT 반환
        return new SuccessResponse<>(tokens);
    }

    @PostMapping("/api/members/exist")
    public ResponseEntity<MemberExistResponse> checkMemberExist(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody MemberExistRequest request
    ) {
        return ResponseEntity.ok(memberService.checkMemberExist(request.phoneNumber()));
    }

    // 닉네임 중복 확인 API
    @GetMapping("/api/auth/register/check/nickname")
    public SuccessResponse<String> checkNickname(@RequestParam String nickname) {
        memberService.checkNicknameDuplication(nickname);
        return new SuccessResponse<>();
    }
}
