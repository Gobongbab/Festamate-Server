package com.gobongbob.festamate.domain.auth.oauth.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Jackson 역직렬화를 위해 필수
@JsonIgnoreProperties(ignoreUnknown = true) // 정의되지 않은 다른 JSON 필드 무시)
public class KakaoUserInfo {

    private Long id; // 필요한 사용자 고유 ID 필드만 정의
}
