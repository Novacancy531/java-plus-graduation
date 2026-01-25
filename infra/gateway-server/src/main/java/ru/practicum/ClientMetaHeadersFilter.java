package ru.practicum;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ClientMetaHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();

        String path = req.getURI().getRawPath();
        String query = req.getURI().getRawQuery();
        final String uri = (query == null || query.isBlank()) ? path : (path + "?" + query);

        String tmpIp = extractClientIp(req);
        final String ip = (tmpIp == null || tmpIp.isBlank()) ? "unknown" : tmpIp;

        ServerHttpRequest mutated = req.mutate()
                .headers(h -> {
                    h.set("X-Client-Ip", ip);
                    h.set("X-Request-Uri", uri);
                })
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private String extractClientIp(ServerHttpRequest req) {
        String xff = req.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        if (req.getRemoteAddress() != null && req.getRemoteAddress().getAddress() != null) {
            return req.getRemoteAddress().getAddress().getHostAddress();
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
