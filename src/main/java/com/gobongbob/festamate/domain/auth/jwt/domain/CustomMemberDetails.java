package com.gobongbob.festamate.domain.auth.jwt.domain;


import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public String getPassword() {
        return member.getLoginPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = member.getRole();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
