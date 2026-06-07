package com.minibill.gateway.filter;

import com.minibill.gateway.util.JwtGatewayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局JWT认证过滤器
 * 白名单路径直接放行，其他路径校验JWT Token
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 白名单路径（不需要登录）
     */
    private static final List<String> WHITE_LIST = List.of(
            "/system/auth/login",
            "/system/auth/register",
            "/system/auth/captcha",
            "/system/auth/sendEmailCode",
            "/bus/api/file/download"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 获取并校验token
        String token = extractToken(request);
        if (token == null) {
            log.warn("请求未携带token: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Long userId = JwtGatewayUtil.getUserId(token);
        if (userId == null) {
            log.warn("token无效或已过期: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 将用户ID传递到下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Name", JwtGatewayUtil.getUsername(token))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith)
                || path.contains("/api/inner/")  // 内部Feign调用放行（实际应做来源校验）
                || path.startsWith("/system/auth/");
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
