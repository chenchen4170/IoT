package com.iot.admin.service;

import com.iot.admin.exception.BizException;
import com.iot.admin.exception.ErrorCode;
import jakarta.annotation.Resource;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private final SecureRandom random = new SecureRandom();

    @Resource
    private StringRedisTemplate redis;

    @Resource
    private JwtService jwtService;

    @Value("${auth.refresh-token-ttl}")
    private Duration refreshTokenTtl;

    public Tokens issue(Long userId, String username) {
        String sessionId = newToken();
        String refresh = userId + "." + newToken();
        String access = jwtService.issue(userId, username, sessionId);
        redis.opsForValue().set(sessionKey(userId), sessionId + ":" + refresh, refreshTokenTtl);
        return new Tokens(access, refresh);
    }

    public Jwt requireActive(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Jwt jwt;
        Long userId;
        try {
            jwt = jwtService.decode(accessToken);
            userId = Long.valueOf(jwt.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String sessionId = jwt.getClaimAsString("sid");
        String current = redis.opsForValue().get(sessionKey(userId));
        if (sessionId == null || current == null || !current.startsWith(sessionId + ":")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return jwt;
    }

    public boolean isRefreshActive(String refreshToken) {
        if (refreshToken == null) {
            return false;
        }
        int separator = refreshToken.indexOf('.');
        if (separator < 1) {
            return false;
        }
        try {
            Long userId = Long.valueOf(refreshToken.substring(0, separator));
            String current = redis.opsForValue().get(sessionKey(userId));
            return current != null && current.endsWith(":" + refreshToken);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String sessionKey(Long userId) {
        return "auth:session:" + userId;
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record Tokens(String access, String refresh) {
    }
}
