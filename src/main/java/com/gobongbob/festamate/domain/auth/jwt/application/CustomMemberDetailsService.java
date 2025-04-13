package com.gobongbob.festamate.domain.auth.jwt.application;


import static com.gobongbob.festamate.global.response.ResponseCode.USER_NOT_FOUND;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        // id로 Member를 찾고, 없으면 예외 처리
        Member member = memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        return new CustomMemberDetails(member);
    }
}
