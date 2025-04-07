package com.gobongbob.festamate.global.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(true, "요청에 성공하였습니다."),
    INTERNAL_SEVER_ERROR(false, "서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    INVALID_REQUEST(false, "올바르지 않은 요청입니다."),


    EXCEED_IMAGE_CAPACITY(false, "업로드 가능한 이미지 용량을 초과했습니다.");

    private final boolean isSuccess;
    private final String message;

    ResponseCode(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
