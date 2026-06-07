package com.minibill.common.util;

import com.minibill.common.constant.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            Constants.TOKEN_SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成JWT token
     */
    public static String generateToken(Long userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Constants.TOKEN_EXPIRATION);

        return Jwts.builder()
                .id(String.valueOf(userId))
                .subject(username)
                .claims(claims != null ? claims : Map.of())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析JWT token
     */
    public static Claims parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String jwtToken = token.startsWith(Constants.TOKEN_PREFIX)
                    ? token.substring(Constants.TOKEN_PREFIX.length())
                    : token;
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? Long.valueOf(claims.getId()) : null;
    }

    /**
     * 从token中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 判断token是否过期
     */
    public static boolean isExpired(String token) {
        Claims claims = parseToken(token);
        return claims == null || claims.getExpiration() == null
                || claims.getExpiration().before(new Date());
    }
}
