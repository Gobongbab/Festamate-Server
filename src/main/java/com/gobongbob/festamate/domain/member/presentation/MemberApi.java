package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.dto.request.MemberExistRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import com.gobongbob.festamate.domain.room.dto.response.MemberExistResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

public interface MemberApi {

    @Operation(summary = "모든 회원 조회", description = "모든 회원 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/members")
    SuccessResponse<List<MemberResponse>> findAllMembers();

    @Operation(summary = "회원 상세 조회", description = "회원 ID로 회원 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "회원이 존재하지 않습니다.")
    })
    @GetMapping("/members/{memberId}")
    SuccessResponse<MemberResponse> findMemberById(
            @Parameter(name = "memberId", description = "회원 ID")
            @PathVariable("memberId") Long memberId
    );

    @Operation(summary = "나의 프로필 조회", description = "나의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @GetMapping("/api/auth/members/profile")
    SuccessResponse<MemberProfileResponse> getProfile(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    );

    @Operation(summary = "나의 프로필 수정", description = "나의 프로필 정보(닉네임)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
    })
    @PatchMapping("/api/auth/members/profile")
    SuccessResponse<Void> updateProfile(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "프로필 수정 요청 정보")
            @RequestBody @Valid ProfileUpdateRequest request
    );

    // 나의 프로필 사진 수정
    @Operation(
            summary = "나의 프로필 사진 수정",
            description = "나의 프로필 사진을 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "object")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PutMapping("/api/auth/members/profile/photo")
    SuccessResponse<Void> updateProfilePhoto(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "프로필 이미지 (단일 파일)")
            @RequestPart("profileImage") MultipartFile profileImage
    );

    @Operation(summary = "회원 삭제", description = "회원 ID로 회원을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "회원이 존재하지 않습니다.")
    })
    @DeleteMapping("/members/{memberId}")
    SuccessResponse<Void> deleteMemberById(
            @Parameter(name = "memberId", description = "삭제할 회원 ID")
            @PathVariable("memberId") Long memberId
    );

    @Operation(summary = "회원 존재 여부 확인", description = "전화번호로 회원 존재 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/api/members/exist")
    SuccessResponse<MemberExistResponse> checkMemberExist(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(description = "전화번호 확인 요청 정보")
            @RequestBody MemberExistRequest request
    );

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다."),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임이 존재합니다.")
    })
    @GetMapping("/api/auth/register/check/nickname")
    SuccessResponse<String> checkNickname(
            @Parameter(name = "nickname", description = "확인할 닉네임", required = true)
            @RequestParam(name = "nickname") String nickname
    );
}
