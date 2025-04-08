package com.gobongbob.festamate.domain.auth.jwt.application;


import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.response.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.gobongbob.festamate.global.response.ResponseCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        return new CustomMemberDetails(member);
    }
}
