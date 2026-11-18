package com.iot.admin.user;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {
    private static final DefaultRedisScript<Long> ROTATE = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end "
                    + "redis.call('DEL', KEYS[1]) "
                    + "redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2]) return 1",
            Long.class);

    private final StringRedisTemplate redis;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public TokenStore(StringRedisTemplate redis,
                      @Value("${auth.access-token-ttl:15m}") Duration accessTtl,
                      @Value("${auth.refresh-token-ttl:7d}") Duration refreshTtl) {
        this.redis = redis;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public void saveAccess(String tokenKey, Long userId) {
        redis.opsForValue().set("auth:access:" + tokenKey, userId.toString(), accessTtl);
    }

    public void saveRefresh(String tokenKey, Long userId) {
        redis.opsForValue().set("auth:refresh:" + tokenKey, userId.toString(), refreshTtl);
    }

    public Long accessUser(String tokenKey) {
        return parseId(redis.opsForValue().get("auth:access:" + tokenKey));
    }

    public Long refreshUser(String tokenKey) {
        return parseId(redis.opsForValue().get("auth:refresh:" + tokenKey));
    }

    public boolean rotateRefresh(String oldKey, String newKey, Long userId) {
        Long result = redis.execute(ROTATE,
                List.of("auth:refresh:" + oldKey, "auth:refresh:" + newKey),
                userId.toString(), Long.toString(refreshTtl.toSeconds()));
        return Long.valueOf(1).equals(result);
    }

    public long accessExpiresIn() {
        return accessTtl.toSeconds();
    }

    private Long parseId(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}
