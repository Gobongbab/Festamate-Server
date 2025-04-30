package com.gobongbob.festamate.domain.auth.jwt.domain;

import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.domain.Member.MemberStatus;
import com.gobongbob.festamate.domain.member.domain.Role;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomMemberDetails implements UserDetails, Serializable, Principal {

    private final Member member;

    public CustomMemberDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = this.member.getRole();
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
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
        return this.member.getStatus() == MemberStatus.ACTIVE;
    }

    @Override
    public String getName() {
        return Optional.ofNullable(member.getNickname())
                .orElse("anonymous-" + UUID.randomUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomMemberDetails that = (CustomMemberDetails) o;
        return Objects.equals(member.getId(), that.member.getId()); // 고유한 사용자 식별자 기준
    }

    @Override
    public int hashCode() {
        return Objects.hash(member.getId());
    }

}
