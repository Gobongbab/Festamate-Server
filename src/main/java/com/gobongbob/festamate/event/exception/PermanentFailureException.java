package com.gobongbob.festamate.event.exception;

public class PermanentFailureException extends RuntimeException {

    public PermanentFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}