package com.iot.admin.service;

import com.iot.admin.exception.BizException;
import com.iot.admin.exception.ErrorCode;
import jakarta.annotation.Resource;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private static final DefaultRedisScript<Long> ROTATE_SESSION = new DefaultRedisScript<>(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2], 'PX', ARGV[3])
                return 1
            end
            return 0
            """, Long.class);

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

    public Long refreshUserId(String refreshToken) {
        if (refreshToken == null || !refreshToken.matches("[1-9][0-9]*\\.[A-Za-z0-9_-]{43}")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.valueOf(refreshToken.substring(0, refreshToken.indexOf('.')));
        } catch (NumberFormatException exception) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    public Tokens refresh(Long userId, String username, String refreshToken) {
        String key = sessionKey(userId);
        String current = redis.opsForValue().get(key);
        if (current == null || !current.endsWith(":" + refreshToken)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String sessionId = newToken();
        String nextRefresh = userId + "." + newToken();
        String access = jwtService.issue(userId, username, sessionId);
        Long changed = redis.execute(ROTATE_SESSION, Collections.singletonList(key),
                current, sessionId + ":" + nextRefresh, String.valueOf(refreshTokenTtl.toMillis()));
        if (!Long.valueOf(1).equals(changed)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return new Tokens(access, nextRefresh);
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
