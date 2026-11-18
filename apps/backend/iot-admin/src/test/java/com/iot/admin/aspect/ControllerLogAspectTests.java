package com.iot.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerLogAspectTests {

    private final ControllerLogAspect aspect = new ControllerLogAspect(new ObjectMapper());

    @Test
    void masksNestedSensitiveValues() {
        String log = aspect.format(Map.of("account", Map.of("password", "hidden-value",
                "apiKey", "hidden-key", "name", "visible")));

        assertTrue(log.contains("\"password\":\"***\""));
        assertTrue(log.contains("\"apiKey\":\"***\""));
        assertTrue(log.contains("\"name\":\"visible\""));
        assertFalse(log.contains("hidden-value"));
        assertFalse(log.contains("hidden-key"));
    }

    @Test
    void truncatesLargeValues() {
        String log = aspect.format(Map.of("description", "a".repeat(3000)));

        assertTrue(log.length() < 2100);
        assertTrue(log.endsWith("...[truncated]"));
    }
}
