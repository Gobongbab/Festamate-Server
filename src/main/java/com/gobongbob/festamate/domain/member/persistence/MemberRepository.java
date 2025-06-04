package com.gobongbob.festamate.domain.member.persistence;

import com.gobongbob.festamate.domain.auth.oauth.domain.OauthInfo;
import com.gobongbob.festamate.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member room);

    @Query("SELECT m FROM Member m WHERE m.deleted = false")
    List<Member> findAll();

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.deleted = false")
    List<Member> findAllWithProfileImage();

    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.deleted = false")
    Optional<Member> findById(Long id);

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.id = :id AND m.deleted = false")
    Optional<Member> findByIdWithProfileImage(Long id);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.profileImage pi WHERE m.name = :memberName AND m.deleted = false")
    List<Member> findAllByNameWithProfileImage(@Param("memberName") String name);

    void delete(Member room); // Soft Delete라면 실제 삭제는 사용하지 않는 것이 일반적이라고 함

    @Query("SELECT m FROM Member m WHERE m.oauthInfo = :oauthInfo AND m.deleted = false")
    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.nickname = :nickname AND m.deleted = false")
    boolean existsByNickname(String nickname); // 닉네임 중복 확인

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.loginId = :loginId AND m.deleted = false")
    Optional<Member> findByLoginIdWithProfileImage(String loginId);

    @Query("SELECT m FROM Member m WHERE m.phoneNumber = :phoneNumber AND m.deleted = false")
    Optional<Member> findByPhoneNumber(String phoneNumber); // 전화번호로 회원 조회

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.phoneNumber = :phoneNumber AND m.deleted = false")
    boolean existsByPhoneNumber(String phoneNumber); // 전화번호 중복 확인

    @Query("SELECT m FROM Member m WHERE m.kakaoId = :kakaoId AND m.deleted = false")
    Optional<Member> findByKakaoId(@Param("kakaoId") Long kakaoId);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.kakaoId = :kakaoId AND m.deleted = false")
    boolean existsByKakaoId(@Param("kakaoId") Long kakaoId);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.studentId = :studentId AND m.deleted = false")
    boolean existsByStudentId(@Param("studentId") String studentId); // 학번 중복 확인

    @Query("SELECT m FROM Member m WHERE m.loginId = :loginId AND m.deleted = false")
    Optional<Member> findByLoginId(@Param("loginId") String loginId); // 일반 로그인 아이디 조회

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.loginId = :loginId AND m.deleted = false")
    boolean existsByLoginId(@Param("loginId") String loginId); // 일반 로그인 아이디 확인

}