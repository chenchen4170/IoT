package com.iot.admin.service;

import jakarta.annotation.Resource;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private final SecureRandom random = new SecureRandom();

    @Resource
    private StringRedisTemplate redis;

    @Value("${auth.access-token-ttl}")
    private Duration accessTokenTtl;

    @Value("${auth.refresh-token-ttl}")
    private Duration refreshTokenTtl;

    public Tokens issue(Long userId) {
        String access = newToken();
        String refresh = newToken();
        redis.opsForValue().set("auth:access:" + access, userId.toString(), accessTokenTtl);
        try {
            redis.opsForValue().set("auth:refresh:" + refresh, userId.toString(), refreshTokenTtl);
        } catch (RuntimeException exception) {
            redis.delete("auth:access:" + access);
            throw exception;
        }
        return new Tokens(access, refresh);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record Tokens(String access, String refresh) {
    }
}
