package com.freesia.metatradegateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Slf4j
@Component
public class WebSocketConnectListener implements ApplicationListener<SessionConnectEvent> {

    private final WebSocketConnCounter counter;

    @Autowired
    public WebSocketConnectListener(WebSocketConnCounter counter) {
        this.counter = counter;
    }

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        counter.increment();
        log.info(String.format("ConnectListener: Current Count: %d", counter.onlineUsers()));
    }

}

