package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final TokenService tokenService;

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/auth/signup")
    public SuccessResponse<Void> signUp(
            @Parameter(description = "회원가입 요청 정보")
            @RequestBody @Valid MemberCreateRequest request
    ) {
        Member member = memberService.createMember(request);
        return new SuccessResponse<>();
    }

    @Operation(summary = "모든 회원 조회", description = "모든 회원 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/members")
    public SuccessResponse<List<MemberResponse>> findAllMembers() {
        return new SuccessResponse<>(memberService.findAllMembers());
    }

    @Operation(summary = "회원 상세 조회", description = "회원 ID로 회원 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "회원이 존재하지 않습니다.")
    })
    @GetMapping("/members/{memberId}")
    public SuccessResponse<MemberResponse> findMemberById(
            @Parameter(name = "memberId", description = "회원 ID")
            @PathVariable("memberId") Long memberId
    ) {
        return new SuccessResponse<>(memberService.findMemberById(memberId));
    }

    @Operation(summary = "나의 프로필 조회", description = "나의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/api/auth/members/profile")
    public SuccessResponse<MemberProfileResponse> getProfile(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(memberService.findProfile(memberDetails.getMember()));
    }

    @Operation(summary = "나의 프로필 수정", description = "나의 프로필 정보(닉네임)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
    })
    @PatchMapping("/members/profile")
    public SuccessResponse<Void> updateProfile(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "프로필 수정 요청 정보")
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);
        return new SuccessResponse<>();
    }

    @Operation(summary = "회원 삭제", description = "회원 ID로 회원을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "회원이 존재하지 않습니다.")
    })
    @DeleteMapping("/members/{memberId}")
    public SuccessResponse<Void> deleteMemberById(
            @Parameter(name = "memberId", description = "삭제할 회원 ID")
            @PathVariable("memberId") Long memberId
    ) {
        memberService.deleteMemberById(memberId);
        return new SuccessResponse<>();
    }

    @Operation(summary = "프로필 등록", description = "회원의 프로필 정보를 등록하고 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
    })
    @PostMapping("/api/auth/register/profile")
    public SuccessResponse<Map<String, String>> registerProfile(
            @Parameter(description = "프로필 등록 요청 정보")
            @RequestBody ProfileRegisterRequest request,
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        Long userId = memberDetails.getMember().getId();
        memberService.registerProfile(request, userId);
        Map<String, String> tokens = tokenService.generateTokens(userId);
        return new SuccessResponse<>(tokens);
    }

    @Operation(summary = "회원 존재 여부 확인", description = "전화번호로 회원 존재 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/api/members/exist")
    public SuccessResponse<MemberExistResponse> checkMemberExist(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "전화번호 확인 요청 정보")
            @RequestBody MemberExistRequest request
    ) {
        return new SuccessResponse<>(memberService.checkMemberExist(request.phoneNumber()));
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
    })
    @GetMapping("/api/auth/register/check/nickname")
    public SuccessResponse<String> checkNickname(
            @Parameter(name = "nickname", description = "확인할 닉네임", required = true)
            @RequestParam(name = "nickname") String nickname
    ) {
        memberService.checkNicknameDuplication(nickname);
        return new SuccessResponse<>();
    }
}