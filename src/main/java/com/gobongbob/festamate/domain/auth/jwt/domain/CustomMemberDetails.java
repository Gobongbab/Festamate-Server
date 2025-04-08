package com.gobongbob.festamate.domain.auth.jwt.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomMemberDetails implements UserDetails {

    private final Member member;

    public CustomMemberDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한이 아직 정의되지 않았을 경우 빈 컬렉션 반환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return member.getLoginPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부 (true: 만료되지 않음)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부 (true: 잠기지 않음)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료 여부 (true: 만료되지 않음)
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부 (true: 활성화됨)
        return true;
    }
}
