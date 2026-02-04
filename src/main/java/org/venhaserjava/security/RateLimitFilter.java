package org.venhaserjava.security;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RateLimitFilter {

    private final Map<String, RateLimitInfo> counters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60000; 

    @RouteFilter(10) 
    void rateLimit(RoutingContext rc) {
        String path = rc.request().path();

        // [SÊNIOR] Bypass para endpoints de infraestrutura e monitoramento
        // Isso impede que o Rate Limit bloqueie o Health Check ou a Documentação
        if (path.startsWith("/q/health") || path.startsWith("/swagger") || path.startsWith("/v1/openapi")) {
            rc.next();
            return;
        }

        String clientIp = rc.request().remoteAddress().host();
        long now = System.currentTimeMillis();

        RateLimitInfo info = counters.compute(clientIp, (key, old) -> {
            if (old == null || (now - old.startTime) > WINDOW_MILLIS) {
                return new RateLimitInfo(now, new AtomicInteger(1));
            }
            old.counter.incrementAndGet();
            return old;
        });

        if (info.counter.get() > MAX_REQUESTS) {
            rc.response()
                .setStatusCode(429) 
                .putHeader("Content-Type", "application/json")
                .end("{\"error\": \"Limite de requisições excedido. Tente novamente em 1 minuto.\"}");
        } else {
            rc.next();
        }
    }

    private static class RateLimitInfo {
        long startTime;
        AtomicInteger counter;

        RateLimitInfo(long startTime, AtomicInteger counter) {
            this.startTime = startTime;
            this.counter = counter;
        }
    }
}


/*
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RateLimitFilter {

    // Armazena o contador por IP
    private final Map<String, RateLimitInfo> counters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60000; // 1 minuto (60.000 ms)

    @RouteFilter(10) // Define a prioridade do filtro (executa cedo)
    void rateLimit(RoutingContext rc) {
        String clientIp = rc.request().remoteAddress().host();
        long now = System.currentTimeMillis();

        RateLimitInfo info = counters.compute(clientIp, (key, old) -> {
            // Se for novo ou se o tempo de 1 minuto já passou, reseta o contador
            if (old == null || (now - old.startTime) > WINDOW_MILLIS) {
                return new RateLimitInfo(now, new AtomicInteger(1));
            }
            old.counter.incrementAndGet();
            return old;
        });

        if (info.counter.get() > MAX_REQUESTS) {
            rc.response()
                .setStatusCode(429) // HTTP 429 Too Many Requests
                .putHeader("Content-Type", "application/json")
                .end("{\"error\": \"Limite de requisições excedido. Tente novamente em 1 minuto.\"}");
        } else {
            rc.next(); // Segue para o Resource (Artista, Album, Auth...)
        }
    }

    private static class RateLimitInfo {
        long startTime;
        AtomicInteger counter;

        RateLimitInfo(long startTime, AtomicInteger counter) {
            this.startTime = startTime;
            this.counter = counter;
        }
    }
}
*/