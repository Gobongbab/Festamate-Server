package com.gobongbob.festamate.domain.auth.jwt.domain;


import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.Collection;
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
        return null;
    }

    @Override
    public String getPassword() {
        return member.getLoginPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }
}
