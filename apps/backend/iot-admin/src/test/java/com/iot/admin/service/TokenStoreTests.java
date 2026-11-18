package com.iot.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iot.admin.exception.BizException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

class TokenStoreTests {

    @Test
    void secondLoginReplacesOnlyThatUsersSession() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        Map<String, String> sessions = new HashMap<>();
        when(redis.opsForValue()).thenReturn(values);
        doAnswer(invocation -> {
            sessions.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(values).set(anyString(), anyString(), any(Duration.class));
        when(values.get(anyString())).thenAnswer(invocation -> sessions.get(invocation.getArgument(0)));

        TokenStore store = new TokenStore();
        ReflectionTestUtils.setField(store, "redis", redis);
        ReflectionTestUtils.setField(store, "jwtService", new JwtService(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15)));
        ReflectionTestUtils.setField(store, "refreshTokenTtl", Duration.ofDays(7));

        TokenStore.Tokens first = store.issue(42L, "alice");
        TokenStore.Tokens otherUser = store.issue(43L, "bob");
        assertEquals("42", store.requireActive(first.access()).getSubject());
        assertTrue(store.isRefreshActive(first.refresh()));

        TokenStore.Tokens second = store.issue(42L, "alice");
        assertNotEquals(first.access(), second.access());
        assertNotEquals(first.refresh(), second.refresh());
        assertEquals("42", new JwtService(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15))
                .decode(first.access()).getSubject());
        assertThrows(BizException.class, () -> store.requireActive(first.access()));
        assertFalse(store.isRefreshActive(first.refresh()));
        assertEquals("42", store.requireActive(second.access()).getSubject());
        assertTrue(store.isRefreshActive(second.refresh()));
        assertEquals("43", store.requireActive(otherUser.access()).getSubject());
        assertTrue(store.isRefreshActive(otherUser.refresh()));
        assertThrows(BizException.class, () -> store.requireActive("not-a-jwt"));
        verify(values, times(2)).set(eq("auth:session:42"), anyString(), eq(Duration.ofDays(7)));
    }

    @Test
    void failedAtomicRotationDoesNotReturnNewTokens() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn("session:42." + "a".repeat(43));
        when(redis.execute(isA(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(0L);

        TokenStore store = new TokenStore();
        ReflectionTestUtils.setField(store, "redis", redis);
        ReflectionTestUtils.setField(store, "jwtService", new JwtService(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", Duration.ofMinutes(15)));
        ReflectionTestUtils.setField(store, "refreshTokenTtl", Duration.ofDays(7));

        assertEquals(42L, store.refreshUserId("42." + "a".repeat(43)));
        assertThrows(BizException.class, () -> store.refresh(42L, "alice", "42." + "a".repeat(43)));
        assertThrows(BizException.class, () -> store.refreshUserId("42.invalid"));
        assertThrows(BizException.class, () -> store.refreshUserId("99999999999999999999." + "a".repeat(43)));
    }
}
