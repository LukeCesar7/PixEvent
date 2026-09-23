package com.pixevent.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limitador de requisições simples em memória (janela fixa), por IP.
 * Equivalente funcional ao express-rate-limit usado no server.js original.
 * Obs: em memória por instância — se você escalar para múltiplas réplicas,
 * troque por um limitador baseado em Redis (ex: Bucket4j + Redis).
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final int max;
    private final long windowMs;
    private final String mensagem;
    private final ConcurrentHashMap<String, Window> janelas = new ConcurrentHashMap<>();

    public RateLimitInterceptor(int max, long windowMs, String mensagem) {
        this.max = max;
        this.windowMs = windowMs;
        this.mensagem = mensagem;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = clientIp(request);
        long now = System.currentTimeMillis();

        Window window = janelas.compute(key, (k, w) -> {
            if (w == null || now - w.startedAt > windowMs) {
                return new Window(now, new AtomicInteger(1));
            }
            w.count.incrementAndGet();
            return w;
        });

        if (window.count.get() > max) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
            return false;
        }

        return true;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Window {
        final long startedAt;
        final AtomicInteger count;

        Window(long startedAt, AtomicInteger count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
