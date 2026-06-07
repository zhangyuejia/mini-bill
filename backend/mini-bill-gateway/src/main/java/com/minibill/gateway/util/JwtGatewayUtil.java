package com.minibill.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关JWT工具类（与common模块保持相同密钥）
 */
@Slf4j
public class JwtGatewayUtil {

    private static final String TOKEN_SECRET = "mini-bill-secret-key-2024-zhang-family-finance";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            TOKEN_SECRET.getBytes(StandardCharsets.UTF_8));

    public static Claims parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String jwtToken = token.startsWith(TOKEN_PREFIX)
                    ? token.substring(TOKEN_PREFIX.length())
                    : token;
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.valueOf(claims.getId()) : null;
    }

    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }
}
