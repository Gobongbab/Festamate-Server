package com.gobongbob.festamate.domain.member.persistence;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member room);

    List<Member> findAll();

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage")
    List<Member> findAllWithProfileImage();

    Optional<Member> findById(Long id);

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.id = :id")
    Optional<Member> findByIdWithProfileImage(Long id);

    void delete(Member room);

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    boolean existsByNickname(String nickname); // 닉네임 중복 확인

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.loginId = ?1")
    Optional<Member> findByLoginIdWithProfileImage(String loginId);

    Optional<Member> findByPhoneNumber(String phoneNumber); // 전화번호로 회원 조회

    boolean existsByPhoneNumber(String phoneNumber); // 전화번호 중복 확인

    Optional<Member> findByKakaoId(Long kakaoId);

    boolean existsByKakaoId(Long kakaoId);

    boolean existsByStudentId(String studentId); // 학번 중복 확인

    Optional<Member> findByLoginId(String loginId); // 일반 로그인 아이디 조회

    boolean existsByLoginId(String loginId); // 일반 로그인 아이디 확인

}