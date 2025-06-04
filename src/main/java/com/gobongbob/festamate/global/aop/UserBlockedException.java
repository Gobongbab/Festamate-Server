package com.gobongbob.festamate.global.aop;

public class UserBlockedException extends RuntimeException {

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException() {
        super("차단된 사용자입니다. 이 기능을 사용할 수 없습니다.");
    }
}
