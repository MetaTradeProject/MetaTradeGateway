package com.freesia.metatradegateway.websocket;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
public class WebSocketConnCounter {

    private final LongAdder connections = new LongAdder();

    public void increment() {
        connections.increment();
    }

    public void decrement() {
        connections.decrement();
    }

    public long onlineUsers() {
        return connections.sum();
    }
}
