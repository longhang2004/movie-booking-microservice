package com.example.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Profile("!test")
public class RedisRateLimitFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitFilter.class);
    private static final int LIMIT = 60;

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator") || path.contains("swagger") || path.contains("api-docs")) {
            return chain.filter(exchange);
        }
        String ip = clientIp(exchange);
        String key = "rl:" + ip;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1L) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > LIMIT) {
                        log.warn("Rate limit exceeded for {}", ip);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        byte[] body = """
                                {"title":"Too Many Requests","status":429,"detail":"Rate limit exceeded"}
                                """.getBytes();
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(ex -> {
                    log.warn("Rate limiter degraded: {}", ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        var addr = exchange.getRequest().getRemoteAddress();
        return addr == null ? "unknown" : addr.getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
