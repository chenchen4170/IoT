package com.iot.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class SqliteDatabaseTests {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void initializesSqliteSchemaAndConstraints() {
        assertEquals("table", jdbc.queryForObject(
                "SELECT type FROM sqlite_master WHERE name = 'iot_user'", String.class));

        jdbc.update("INSERT INTO iot_user(username, password_hash) VALUES (?, ?)", "tester", "hash");
        assertThrows(DataAccessException.class, () ->
                jdbc.update("INSERT INTO iot_user(username, password_hash) VALUES (?, ?)",
                        "TESTER", "hash"));

        String createdAt = jdbc.queryForObject(
                "SELECT create_at FROM iot_user WHERE username = ?", String.class, "tester");
        assertTrue(createdAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));

        String updatedAt = jdbc.queryForObject(
                "SELECT update_at FROM iot_user WHERE username = ?", String.class, "tester");
        assertTrue(updatedAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
    }
}
