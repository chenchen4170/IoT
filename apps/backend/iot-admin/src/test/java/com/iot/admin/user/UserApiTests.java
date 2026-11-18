package com.iot.admin.user;

import java.util.HashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.iot.admin.user.service.TokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean TokenStore tokens;

    private final Map<String, Long> access = new HashMap<>();
    private final Map<String, Long> refresh = new HashMap<>();

    @BeforeEach
    void mockTokenStorage() {
        doAnswer(call -> {
            access.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(tokens).saveAccess(any(), any());
        doAnswer(call -> {
            refresh.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(tokens).saveRefresh(any(), any());
        when(tokens.accessUser(any())).thenAnswer(call -> access.get(call.getArgument(0)));
        when(tokens.refreshUser(any())).thenAnswer(call -> refresh.get(call.getArgument(0)));
        when(tokens.rotateRefresh(any(), any(), any())).thenAnswer(call -> {
            String oldKey = call.getArgument(0);
            String newKey = call.getArgument(1);
            Long userId = call.getArgument(2);
            if (!userId.equals(refresh.remove(oldKey))) {
                return false;
            }
            refresh.put(newKey, userId);
            return true;
        });
        when(tokens.accessExpiresIn()).thenReturn(900L);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void registerLoginRefreshAndProtectProfile(CapturedOutput output) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"Test@Example.com","password":"correct-horse-123","displayName":"Test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value(0))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
        String hash = jdbc.queryForObject("SELECT password_hash FROM iot_user WHERE email = ?",
                String.class, "test@example.com");
        assertTrue(hash.startsWith("$2"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"TEST@example.com","password":"correct-horse-123"}
                                """))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"TEST@example.com","password":"correct-horse-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(900))
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(body, "$.data.accessToken");
        String refreshToken = JsonPath.read(body, "$.data.refreshToken");
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        assertTrue(!output.getOut().contains(accessToken));
        assertTrue(!output.getOut().contains(refreshToken));
        mvc.perform(patch("/api/users/me").header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Updated"));

        String next = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertNotEquals(accessToken, JsonPath.read(next, "$.data.accessToken"));
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminCanManageUsersAndDisabledUsersLoseAccess() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
        String userToken = login("user@example.com");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        jdbc.update("UPDATE iot_user SET role = 1 WHERE email = ?", "admin@example.com");
        String adminToken = login("admin@example.com");
        Long userId = jdbc.queryForObject("SELECT id FROM iot_user WHERE email = ?",
                Long.class, "user@example.com");
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mvc.perform(get("/api/admin/users?page=0").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/users/" + userId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
        mvc.perform(patch("/api/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));
        mvc.perform(patch("/api/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.accessToken");
    }
}
