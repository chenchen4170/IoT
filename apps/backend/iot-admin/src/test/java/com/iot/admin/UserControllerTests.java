package com.iot.admin;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.iot.admin.service.JwtService;
import com.iot.admin.service.TokenStore;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(UserControllerTests.ProtectedController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private StringRedisTemplate redis;

    private final Map<String, String> sessions = new HashMap<>();

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        doAnswer(invocation -> {
            sessions.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(values).set(anyString(), anyString(), any(Duration.class));
        when(values.get(anyString())).thenAnswer(invocation -> sessions.get(invocation.getArgument(0)));
        when(redis.execute(isA(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String key = ((java.util.List<String>) invocation.getArgument(1)).get(0);
                    String expected = invocation.getArgument(2);
                    if (!expected.equals(sessions.get(key))) {
                        return 0L;
                    }
                    sessions.put(key, invocation.getArgument(3));
                    return 1L;
                });
    }

    @Test
    void registersAndLogsIn() throws Exception {
        mvc.perform(post("/api/admin/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"secret-123","confirmPassword":"secret-123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value("Alice"))
                .andExpect(jsonPath("$.data.createAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updateAt").isNotEmpty())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        String hash = jdbc.queryForObject(
                "SELECT password_hash FROM iot_user WHERE username = ?", String.class, "Alice");
        assertNotEquals("secret-123", hash);
        assertTrue(new BCryptPasswordEncoder().matches("secret-123", hash));

        String first = mvc.perform(post("/api/admin/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value("Alice"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String firstToken = JsonPath.read(first, "$.data.token");
        String firstRefresh = JsonPath.read(first, "$.data.refreshToken");
        assertTrue(jwtService.decode(firstToken).getExpiresAt().isAfter(Instant.now()));
        assertTrue(tokenStore.isRefreshActive(firstRefresh));
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
                .andExpect(status().isOk());

        String second = mvc.perform(post("/api/admin/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"secret-123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondToken = JsonPath.read(second, "$.data.token");
        String secondRefresh = JsonPath.read(second, "$.data.refreshToken");
        assertNotEquals(firstToken, secondToken);
        assertFalse(tokenStore.isRefreshActive(firstRefresh));
        assertTrue(tokenStore.isRefreshActive(secondRefresh));
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/user/protected")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidRegistrationAndDuplicateUsername() throws Exception {
        mvc.perform(post("/api/admin/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret-123","confirmPassword":"different"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("Passwords do not match"));

        mvc.perform(post("/api/admin/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret-123","confirmPassword":"secret-123"}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/admin/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ALICE","password":"secret-123","confirmPassword":"secret-123"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidCredentialsWithoutRevealingUsername() throws Exception {
        String body = "{\"username\":\"unknown\",\"password\":\"wrong-password\"}";
        mvc.perform(post("/api/admin/user/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("Unauthorized"));
        mvc.perform(get("/api/admin/health")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/user/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void refreshRotatesTokensAndRejectsReplay() throws Exception {
        mvc.perform(post("/api/admin/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"secret-123","confirmPassword":"secret-123"}
                                """))
                .andExpect(status().isCreated());
        String login = mvc.perform(post("/api/admin/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice","password":"secret-123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String oldToken = JsonPath.read(login, "$.data.token");
        String oldRefresh = JsonPath.read(login, "$.data.refreshToken");

        String refreshed = mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String newToken = JsonPath.read(refreshed, "$.data.token");
        String newRefresh = JsonPath.read(refreshed, "$.data.refreshToken");
        assertNotEquals(oldToken, newToken);
        assertNotEquals(oldRefresh, newRefresh);
        assertFalse(tokenStore.isRefreshActive(oldRefresh));
        assertTrue(tokenStore.isRefreshActive(newRefresh));
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk());
        mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + newRefresh + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/user/protected").header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"bad-token\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"999999." + "a".repeat(43) + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/user/refreshToken").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @RestController
    static class ProtectedController {
        @GetMapping("/api/admin/user/protected")
        String protectedEndpoint() {
            return "ok";
        }
    }
}
