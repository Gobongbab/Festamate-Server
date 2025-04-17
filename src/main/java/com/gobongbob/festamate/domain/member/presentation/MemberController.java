package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final TokenService tokenService;

//    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
//            @ApiResponse(responseCode = "400", description = "잘못된 요청")
//    })
//    @PostMapping("/auth/signup")
//    public SuccessResponse<Void> signUp(
//            @Parameter(description = "회원가입 요청 정보")
//            @RequestBody @Valid MemberCreateRequest request
//    ) {
//        Member member = memberService.createMember(request);
//        return new SuccessResponse<>();
//    }

    @Override
    @GetMapping("/members")
    public SuccessResponse<List<MemberResponse>> findAllMembers() {
        return new SuccessResponse<>(memberService.findAllMembers());
    }

    @Override
    @GetMapping("/members/{memberId}")
    public SuccessResponse<MemberResponse> findMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        return new SuccessResponse<>(memberService.findMemberById(memberId));
    }

    @Override
    @GetMapping("/api/auth/members/profile")
    public SuccessResponse<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(memberService.findProfile(memberDetails.getMember()));
    }

    @Override
    @PatchMapping("/members/profile")
    public SuccessResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);
        return new SuccessResponse<>();
    }

    @Override
    @DeleteMapping("/members/{memberId}")
    public SuccessResponse<Void> deleteMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        memberService.deleteMemberById(memberId);
        return new SuccessResponse<>();
    }

//    @Operation(summary = "프로필 등록", description = "회원의 프로필 정보를 등록하고 토큰을 발급합니다.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
//            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
//    })
//    @PostMapping("/api/auth/register/profile") // 추후 /api/auth를 상위 경로에 작성하도록 변경 필요
//    public SuccessResponse<Map<String, String>> registerProfile(
//            @Parameter(description = "프로필 등록 요청 정보")
//            @RequestBody ProfileRegisterRequest request,
//            @Parameter(description = "인증된 사용자 정보", hidden = true)
//            @AuthenticationPrincipal CustomMemberDetails memberDetails // 최소 JWT 정보
//    ) {
//        Long userId = memberDetails.getMember().getId();
//        memberService.registerProfile(request, userId);
//
//        // TokenService를 이용하여 최종 JWT(access, refresh) 생성
//        Map<String, String> tokens = tokenService.generateTokens(userId);
//        // 최종 JWT 반환
//        return new SuccessResponse<>(tokens);
//    }

    @Override
    @PostMapping("/api/members/exist")
    public SuccessResponse<MemberExistResponse> checkMemberExist(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody MemberExistRequest request
    ) {
        return new SuccessResponse<>(memberService.checkMemberExist(request.phoneNumber()));
    }

    @Override
    @GetMapping("/api/auth/register/check/nickname")
    public SuccessResponse<String> checkNickname(
            @RequestParam(name = "nickname") String nickname
    ) {
        memberService.checkNicknameDuplication(nickname);
        return new SuccessResponse<>();
    }
}