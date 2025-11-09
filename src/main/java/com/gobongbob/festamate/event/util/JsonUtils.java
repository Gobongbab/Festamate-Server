package com.gobongbob.festamate.event.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtils {

    private final ObjectMapper objectMapper; // 주입받은 싱글톤 ObjectMapper 사용

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // 실무에서는 적절한 커스텀 예외로 래핑하여 던집니다.
            throw new RuntimeException("object to JSON 변환 실패", e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            // 실제 Jackson readValue 호출
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON to object 변환 실패", e);
        }
    }
}