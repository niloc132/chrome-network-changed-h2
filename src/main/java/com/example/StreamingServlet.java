package com.example;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamingServlet extends HttpServlet {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync(req, resp);
        resp.setCharacterEncoding("utf-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");

        asyncContext.setTimeout(TimeUnit.DAYS.toMillis(1));

        ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
        AtomicInteger msgCount = new AtomicInteger();

        ScheduledFuture<?> f = executorService.scheduleAtFixedRate(() -> {
            try {
                if (outputStream.isReady()) {
                    outputStream.write(String.valueOf(msgCount.getAndIncrement()).getBytes(StandardCharsets.UTF_8));
                } else {
                    System.out.println("not ready!");
                }
                if (outputStream.isReady()) {
                    outputStream.flush();
                } else {
                    System.out.println("not ready!");
                }
            } catch (Throwable e) {
                asyncContext.complete();
            }
        }, 1, 1, TimeUnit.SECONDS);

        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                f.cancel(true);
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                f.cancel(true);
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                f.cancel(true);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

            }
        });
    }
}
