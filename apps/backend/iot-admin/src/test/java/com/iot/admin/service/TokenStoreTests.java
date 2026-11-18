package com.iot.admin.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

class TokenStoreTests {

    @Test
    void issuesDifferentRandomTokensWithConfiguredTtls() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);

        TokenStore store = new TokenStore();
        ReflectionTestUtils.setField(store, "redis", redis);
        ReflectionTestUtils.setField(store, "accessTokenTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(store, "refreshTokenTtl", Duration.ofDays(7));

        TokenStore.Tokens tokens = store.issue(42L);

        assertNotEquals(tokens.access(), tokens.refresh());
        assertTrue(tokens.access().matches("[A-Za-z0-9_-]{43}"));
        assertTrue(tokens.refresh().matches("[A-Za-z0-9_-]{43}"));
        verify(values).set(eq("auth:access:" + tokens.access()), eq("42"), eq(Duration.ofMinutes(15)));
        verify(values).set(eq("auth:refresh:" + tokens.refresh()), eq("42"), eq(Duration.ofDays(7)));
    }
}
