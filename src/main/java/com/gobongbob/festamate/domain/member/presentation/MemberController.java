package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.application.TokenService;
import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.dto.request.MemberExistRequest;
import com.gobongbob.festamate.domain.member.dto.request.MemberFcmTokenRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    // 모든 회원 조회
    @Override
    @GetMapping("/members")
    public SuccessResponse<List<MemberResponse>> findAllMembers() {
        return new SuccessResponse<>(memberService.findAllMembers());
    }

    // 회원 상세 조회
    @Override
    @GetMapping("/members/{memberId}")
    public SuccessResponse<MemberResponse> findMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        return new SuccessResponse<>(memberService.findMemberById(memberId));
    }

    // 프로필 조회
    @Override
    @GetMapping("/api/auth/members/profile")
    public SuccessResponse<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        return new SuccessResponse<>(memberService.findProfile(memberDetails.getMember()));
    }

    // 나의 프로필 수정(닉네임)
    @Override
    @PatchMapping("/api/auth/members/profile")
    public SuccessResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);
        return new SuccessResponse<>();
    }

    // 나의 프로필 사진 수정
    @Override
    @PutMapping("/api/auth/members/profile/photo")
    public SuccessResponse<Void> updateProfilePhoto(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        memberService.updateProfilePhoto(memberDetails.getMember(), profileImage);
        return new SuccessResponse<>();
    }

    // 회원 삭제
    @Override
    @DeleteMapping("/members/{memberId}")
    public SuccessResponse<Void> deleteMemberById(
            @PathVariable("memberId") Long memberId
    ) {
        memberService.deleteMemberById(memberId);
        return new SuccessResponse<>();
    }

    // 회원 존재 여부 확인
    @Override
    @PostMapping("/api/members/exist")
    public SuccessResponse<MemberExistResponse> checkMemberExist(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody MemberExistRequest request
    ) {
        return new SuccessResponse<>(memberService.checkMemberExist(request.phoneNumber()));
    }

    // 닉네임 중복 확인
    @Override
    @GetMapping("/api/auth/register/check/nickname")
    public SuccessResponse<String> checkNickname(
            @RequestParam(name = "nickname") String nickname
    ) {
        memberService.checkNicknameDuplication(nickname);
        return new SuccessResponse<>();
    }

    // FCM 토큰 등록
    @Override
    @PostMapping("/members/fcm-token")
    public SuccessResponse<Void> registerFcmToken(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody MemberFcmTokenRequest request
    ) {
        memberService.registerFcmToken(memberDetails.getMember(), request);
        return new SuccessResponse<>();
    }
}