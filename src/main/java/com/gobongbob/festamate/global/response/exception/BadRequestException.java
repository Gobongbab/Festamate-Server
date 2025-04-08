package com.gobongbob.festamate.global.response.exception;


import com.gobongbob.festamate.global.response.ResponseCode;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String message;

    public BadRequestException(final ResponseCode responseCode) {
        this.message = responseCode.getMessage();
    }
}