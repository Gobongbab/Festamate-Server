package com.gobongbob.festamate.global.config;

import com.gobongbob.festamate.event.handler.EventHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHandlerConfig {

    @Bean
    public Map<String, EventHandler> eventHandlerMap(List<EventHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(EventHandler::getEventType, Function.identity()));
    }
}