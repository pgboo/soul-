package com.soul.social.security;

import com.soul.social.common.constant.CommonConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 签发 / 解析工具（jjwt 0.12.x API）。
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_TYPE = "type";

    private final SecretKey secretKey;

    /** 用户端有效期（天） */
    private final long miniExpireDays;

    /** 后台有效期（天） */
    private final long adminExpireDays;

    public JwtUtil(
            @Value("${soul.jwt.secret}") String secret,
            @Value("${soul.jwt.mini-expire-days:7}") long miniExpireDays,
            @Value("${soul.jwt.admin-expire-days:1}") long adminExpireDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.miniExpireDays = miniExpireDays;
        this.adminExpireDays = adminExpireDays;
    }

    /**
     * 签发用户端 token。
     */
    public String generateMiniToken(Long userId) {
        return generate(userId, CommonConstant.TOKEN_TYPE_MINI, miniExpireDays);
    }

    /**
     * 签发后台 token。
     */
    public String generateAdminToken(Long userId) {
        return generate(userId, CommonConstant.TOKEN_TYPE_ADMIN, adminExpireDays);
    }

    private String generate(Long userId, String type, long expireDays) {
        long now = System.currentTimeMillis();
        long ttl = expireDays * 24L * 60L * 60L * 1000L;
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token，失败（签名错误/过期/格式错误）返回 null。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 token 取 userId，解析失败返回 null。
     */
    public Long getUserId(String token) {
        Claims claims = parse(token);
        if (claims == null) {
            return null;
        }
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 取 token 类型（mini/admin）。
     */
    public String getType(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get(CLAIM_TYPE, String.class);
    }

    /**
     * token 剩余有效秒数（用于登出黑名单 TTL）；已过期返回 0。
     */
    public long getRemainingSeconds(String token) {
        Claims claims = parse(token);
        if (claims == null || claims.getExpiration() == null) {
            return 0L;
        }
        long remain = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
        return Math.max(remain, 0L);
    }
}
