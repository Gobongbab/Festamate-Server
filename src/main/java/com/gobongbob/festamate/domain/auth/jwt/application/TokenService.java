package com.gobongbob.festamate.domain.auth.jwt.application;

import com.gobongbob.festamate.domain.auth.jwt.domain.RefreshToken;
import com.gobongbob.festamate.domain.auth.jwt.domain.TokenType;
import com.gobongbob.festamate.domain.auth.jwt.persistence.RefreshTokenRepository;
import com.gobongbob.festamate.domain.member.application.MemberService;
import com.gobongbob.festamate.domain.member.domain.Member;
import com.gobongbob.festamate.domain.member.persistence.MemberRepository;
import com.gobongbob.festamate.global.util.TokenProvider;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// JWT 토큰 생성, 재발급 및 Redis 연동 관리를 담당하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class TokenService {

    // === Logger 인스턴스 생성 (클래스 상단에 추가) ===
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final TokenProvider tokenProvider;
    private final MemberService memberService; // MemberService 주입 확인
    private final RefreshTokenRepository refreshRepository;
    private final MemberRepository memberRepository; // MemberRepository 주입 확인

    // 로그인 시 최초 토큰 생성 및 리프레시 토큰 Redis 저장
    @Transactional // DB 조회(Member) 및 Redis 저장(Token)
    public Map<String, String> generateAndSaveTokens(Long memberId, TokenType accessType) {
        log.info("최초 토큰 생성 및 저장 시작. 사용자 ID: {}", memberId);
        try {
            Member member = memberService.findById(memberId); // DB에서 사용자 조회
            log.debug("DB에서 사용자 조회 성공. 사용자: {}", member.getId());

            Map<String, String> tokens = tokenProvider.generateTokens(member, accessType); // 토큰 생성
            log.debug("JWT 토큰 생성 완료.");

            String refreshTokenValue = tokens.get("refreshToken");
            saveRefreshTokenToRedis(String.valueOf(memberId),
                    refreshTokenValue); // 리프레시 토큰 Redis에 저장

            log.info("최초 토큰 생성 및 저장 완료. 사용자 ID: {}", memberId);
            return tokens; // 생성된 토큰들 반환
        } catch (Exception e) {
            log.error("최초 토큰 생성 및 저장 중 예외 발생! 사용자 ID: {}", memberId, e);
            throw e; // 예외를 다시 던져서 상위에서 처리하도록 함
        }
    }

    // 액세스 토큰 재발급 (Refresh Token Rotation 적용)
    @Transactional // Redis 조회/삭제/저장 및 DB 조회
    public TokenRefreshResult regenerateAccessToken(String refreshTokenValueFromCookie) {
        // --- 로그 추가 1: 메서드 시작 및 입력 값 확인 ---
        log.info("액세스 토큰 재발급 시도 시작. 전달받은 쿠키 값(앞 5자리): {}",
                (refreshTokenValueFromCookie != null && refreshTokenValueFromCookie.length() > 5) ?
                        refreshTokenValueFromCookie.substring(0, 5) + "..." : "N/A");

        try {
            // 1. Redis에서 리프레시 토큰 조회 (값으로 조회)
            log.debug("Redis에서 리프레시 토큰 조회 시도. 값(앞 5자리): {}",
                    (refreshTokenValueFromCookie != null
                            && refreshTokenValueFromCookie.length() > 5) ?
                            refreshTokenValueFromCookie.substring(0, 5) + "..." : "N/A");

            RefreshToken foundRefreshToken = refreshRepository.findByRefreshTokenValue(
                            refreshTokenValueFromCookie)
                    .orElseThrow(() -> {
                        // --- 로그 추가 2: Redis 조회 실패 시 원인 기록 (WARN 레벨) ---
                        log.warn("Redis에서 리프레시 토큰을 찾을 수 없음. 쿠키 값(앞 5자리): {}",
                                (refreshTokenValueFromCookie != null
                                        && refreshTokenValueFromCookie.length() > 5) ?
                                        refreshTokenValueFromCookie.substring(0, 5) + "..."
                                        : "N/A");
                        // 중요: 이 예외 메시지가 클라이언트에게 노출될 수 있으므로 너무 상세하지 않게 하는 것이 좋음
                        return new IllegalArgumentException("유효하지 않거나 만료된 리프레시 토큰입니다.");
                    });

            // --- 로그 추가 3: Redis 조회 성공 ---
            log.debug("Redis에서 토큰 찾음. 연결된 사용자 ID(Redis Key): {}", foundRefreshToken.getId());

            // 2. 사용자 ID 추출 (Redis Key 값 사용) 및 Member 객체 조회
            String userIdString = foundRefreshToken.getId(); // Redis Key (String 타입 사용자 ID)
            Long memberId = parseLongUserId(userIdString); // DB 조회를 위해 Long으로 변환

            log.debug("DB에서 사용자 조회 시도. ID: {}", memberId);
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> {
                        // --- 로그 추가 4: DB 조회 실패 시 원인 기록 (WARN 레벨) ---
                        log.warn("DB에서 토큰에 해당하는 사용자를 찾을 수 없음. ID: {}", memberId);
                        return new IllegalArgumentException("토큰에 해당하는 사용자를 찾을 수 없습니다."); // ID 노출 제거
                    });
            log.debug("DB에서 사용자 찾음: {}", member.getId());

            // 3. 기존 리프레시 토큰 Redis에서 삭제 (RTR)
            log.debug("기존 리프레시 토큰 삭제 시도. 대상 사용자 ID: {}", userIdString);
            refreshRepository.delete(foundRefreshToken);
            log.debug("기존 리프레시 토큰 삭제 완료.");

            // 4. 새로운 액세스 토큰 및 리프레시 토큰 생성
            log.debug("새 토큰 생성 시도. 사용자: {}", member.getId());
            Map<String, String> newTokens = tokenProvider.generateTokens(member,
                    TokenType.FINAL_ACCESS);
            String newAccessToken = newTokens.get("accessToken");
            String newRefreshTokenValue = newTokens.get("refreshToken");
            log.debug("새 토큰 생성 완료.");

            // 5. 새로운 리프레시 토큰 Redis에 저장 (새 TTL 적용됨)
            log.debug("새 리프레시 토큰 Redis 저장 시도. 사용자 ID: {}, 새 토큰 값(앞 5자리): {}",
                    userIdString,
                    (newRefreshTokenValue != null && newRefreshTokenValue.length() > 5) ?
                            newRefreshTokenValue.substring(0, 5) + "..." : "N/A");
            saveRefreshTokenToRedis(userIdString, newRefreshTokenValue); // String 타입 ID 사용
            log.debug("새 리프레시 토큰 Redis 저장 완료.");

            // 6. 결과 반환 (새 액세스 토큰 + 새 리프레시 토큰)
            log.info("액세스 토큰 재발급 성공. 사용자: {}", member.getId());
            return new TokenRefreshResult(newAccessToken, newRefreshTokenValue);

        } catch (IllegalArgumentException e) {
            // --- 로그 추가 5: 예상된 예외 발생 시 에러 로그 (ERROR 레벨) ---
            log.error("토큰 재발급 실패 (IllegalArgumentException): {}", e.getMessage());
            throw e; // 예외를 다시 던져서 컨트롤러에서 401 처리하도록 함
        } catch (Exception e) {
            // --- 로그 추가 6: 예상치 못한 예외 발생 시 에러 로그 (ERROR 레벨) + 스택 트레이스! ---
            log.error("토큰 재발급 중 예상치 못한 예외 발생!", e); // ⭐ 스택 트레이스를 포함하여 전체 에러 기록
            // 예상치 못한 오류는 500 Internal Server Error 로 처리하는 것이 더 적절할 수 있음
            // 여기서는 일단 IllegalArgumentException으로 감싸서 컨트롤러의 401 처리를 따르도록 함
            throw new IllegalArgumentException("토큰 재발급 중 서버 오류가 발생했습니다.", e);
        }
    }

    // Redis에 리프레시 토큰 저장 (로그인 및 재발급 시 사용)
    private void saveRefreshTokenToRedis(String userId, String refreshTokenValue) {
        log.debug("Redis 저장 로직 실행: 사용자 ID={}, 토큰 값(앞 5)={}", userId,
                (refreshTokenValue != null && refreshTokenValue.length() > 5) ?
                        refreshTokenValue.substring(0, 5) + "..." : "N/A");
        RefreshToken token = RefreshToken.builder()
                .id(userId) // String 타입 ID
                .refreshTokenValue(refreshTokenValue)
                // .ttl(14 * 24 * 60 * 60) // TTL 설정이 @RedisHash에 있다면 여기서 필요 없을 수 있음
                .build();
        try {
            refreshRepository.save(token); // Redis에 저장 (TTL 자동 적용 확인 필요)
            log.debug("Redis 저장 성공: 사용자 ID={}", userId);
        } catch (Exception e) {
            log.error("Redis 저장 실패! 사용자 ID: {}", userId, e);
            // 저장 실패 시 어떻게 처리할지 정책 필요 (예: 예외 던지기)
            throw new RuntimeException("리프레시 토큰 저장 중 오류 발생", e);
        }
    }

    // 로그아웃 시 Redis에서 리프레시 토큰 삭제
    @Transactional
    public void deleteRefreshTokenFromRedis(String userId) {
        log.info("리프레시 토큰 삭제 시도. 사용자 ID: {}", userId);
        try {
            // CrudRepository<RefreshToken, String> 이므로 String ID 전달
            refreshRepository.deleteById(userId); // Redis에서 해당 ID의 데이터 삭제
            log.info("리프레시 토큰 삭제 완료. 사용자 ID: {}", userId);
        } catch (Exception e) {
            log.error("리프레시 토큰 삭제 실패! 사용자 ID: {}", userId, e);
            // 삭제 실패 시 어떻게 처리할지 정책 필요
        }
    }

    // Redis Key(String)에서 가져온 사용자 ID를 Long 타입으로 변환하는 헬퍼 메서드
    private Long parseLongUserId(String userIdString) {
        log.debug("사용자 ID 파싱 시도. 값: {}", userIdString);
        try {
            return Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            // --- 로그 추가 7: 파싱 실패 시 에러 로그 ---
            log.error("사용자 ID 파싱 실패! 값: {}", userIdString, e);
            throw new IllegalArgumentException("Redis 키의 사용자 ID 형식이 올바르지 않습니다: " + userIdString, e);
        }
    }

    // 토큰 재발급 결과를 담는 내부 클래스 (또는 별도 DTO 파일로 분리 가능)
    @Getter
    @RequiredArgsConstructor
    public static class TokenRefreshResult {

        private final String accessToken; // 새로 발급된 액세스 토큰
        private final String refreshToken; // 새로 발급된 리프레시 토큰 (쿠키 설정을 위해 Controller에 전달)
    }
}
