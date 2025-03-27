package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileRegisterRequest;
import com.gobongbob.festamate.domain.member.dto.request.ProfileUpdateRequest;
import com.gobongbob.festamate.domain.member.dto.response.MemberProfileResponse;
import com.gobongbob.festamate.domain.member.dto.response.MemberResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> signUp(@RequestBody MemberCreateRequest request) {
        Member member = memberService.createMember(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> findAllMembers() {
        return ResponseEntity.ok(memberService.findAllMembers());
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberResponse> findMemberById(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.findMemberById(memberId));
    }

    @GetMapping("/api/auth/members/profile")
    public ResponseEntity<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        return ResponseEntity.ok(memberService.findProfile(memberDetails.getMember()));
    }

    @PatchMapping("/members/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestBody ProfileUpdateRequest request
    ) {
        memberService.updateMemberProfileById(memberDetails.getMember(), request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable Long memberId) {
        memberService.deleteMemberById(memberId);

        return ResponseEntity.ok().build();
    }

    // 프로필 등록 API
    @PostMapping("/api/auth/register/profile") // 추후 /api/auth를 상위 경로에 작성하도록 변경 필요
    public ResponseEntity<Void> registerProfile(@RequestBody ProfileRegisterRequest request,
            @AuthenticationPrincipal Member member) {
        Long userId = member.getId();
        memberService.registerProfile(request, userId);
        return ResponseEntity.ok().build();
    }
}
