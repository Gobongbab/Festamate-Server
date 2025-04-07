package com.gobongbob.festamate.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"isSuccess", "message", "result"})
// 이 어노테이션을 클래스 레벨에 적용하여 JSON으로 변환될 때 필드들이 특정한 순서로 나타나도록 설정
public class SuccessResponse<T> {

    private final Boolean isSuccess;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)// 어노테이션이 적용된 필드가 null인 경우, JSON 응답에서 해당 필드는 제외
    private T result;

    // 일반적인 응답 형식
    public SuccessResponse() {
        this.isSuccess = ResponseCode.SUCCESS.isSuccess();
        this.message = ResponseCode.SUCCESS.getMessage();
    }

    // 반환값이 필요한 응답 형식
    public SuccessResponse(T result) {
        this.isSuccess = ResponseCode.SUCCESS.isSuccess();
        this.message = ResponseCode.SUCCESS.getMessage();
        this.result = result;
    }
}