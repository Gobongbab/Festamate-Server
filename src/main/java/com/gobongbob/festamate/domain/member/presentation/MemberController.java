package com.gobongbob.festamate.domain.member.presentation;

import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.dto.request.MemberCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody MemberCreateRequest request) {
        Member member = memberService.createMember(request);

        return ResponseEntity.ok().build();
    }
}
