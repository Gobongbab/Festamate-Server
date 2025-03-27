package com.gobongbob.festamate.domain.member.persistence;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member room);

    List<Member> findAll();

    Optional<Member> findById(Long id);

    void delete(Member room);

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    boolean existsByNickname(String nickname); // 닉네임 중복 확인

    Optional<Member> findByLoginId(String loginId);
}
