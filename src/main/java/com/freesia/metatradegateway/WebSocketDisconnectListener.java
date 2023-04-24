package com.freesia.metatradegateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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
        System.out.println("disconnected");
    }
}
