package com.example;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/websocket")
public class StreamingWebsocket {
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final AtomicInteger msgCount = new AtomicInteger();

    private Session session;
    private ScheduledFuture<?> f;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        f = executorService.scheduleAtFixedRate(this::send, 1, 1, TimeUnit.SECONDS);
    }

    @OnClose
    public void onClose() {
        f.cancel(true);
    }

    private void send() {
        try {
            session.getBasicRemote().sendText(String.valueOf(msgCount.getAndIncrement()));
        } catch (IOException e) {
            f.cancel(true);
        }
    }
}
