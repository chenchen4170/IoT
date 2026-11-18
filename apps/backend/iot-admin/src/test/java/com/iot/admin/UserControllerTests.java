package com.iot.admin;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iot.admin.service.TokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private TokenStore tokenStore;

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

        when(tokenStore.issue(anyLong())).thenReturn(new TokenStore.Tokens("access-value", "refresh-value"));
        mvc.perform(post("/api/admin/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value("Alice"))
                .andExpect(jsonPath("$.data.token").value("access-value"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-value"));
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
    }
}
