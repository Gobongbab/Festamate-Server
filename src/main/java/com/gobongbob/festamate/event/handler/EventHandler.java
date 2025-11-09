package com.gobongbob.festamate.event.handler;

public interface EventHandler {

    String getEventType();

    void handle(String payload);
}
