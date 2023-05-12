package com.freesia.metatradegateway.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final WebSocketConnCounter counter;

    @Autowired
    public WebSocketDisconnectListener(WebSocketConnCounter counter) {
        this.counter = counter;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        counter.decrement();
        log.info(String.format("DisconnectListener: Current Count: %d", counter.onlineUsers()));
    }
}
