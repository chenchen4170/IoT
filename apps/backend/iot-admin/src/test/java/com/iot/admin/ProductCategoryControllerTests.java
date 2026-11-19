package com.iot.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iot.admin.service.JwtService;
import com.iot.admin.service.TokenStore;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductCategoryControllerTests {

    private static final String PATH = "/api/admin/product-category";
    private static final String CATEGORY = """
            {"categoryCode":"sensor","categoryName":"Sensors","description":"IoT sensors"}
            """;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TokenStore tokenStore;

    private String token;

    @BeforeEach
    void authenticate() {
        token = jwtService.issue(42L, "tester", "session");
        when(tokenStore.requireActive(token)).thenReturn(jwtService.decode(token));
    }

    @Test
    void createsRootAndChildWithAuthenticatedAuditorAndUtcTimestamps() throws Exception {
        Instant before = Instant.now().minusSeconds(1);
        String response = create(CATEGORY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.parentId").value(0))
                .andExpect(jsonPath("$.data.categoryCode").value("sensor"))
                .andExpect(jsonPath("$.data.categoryName").value("Sensors"))
                .andExpect(jsonPath("$.data.description").value("IoT sensors"))
                .andExpect(jsonPath("$.data.sortOrder").value(0))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.createdBy").value(42))
                .andExpect(jsonPath("$.data.updatedBy").value(42))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.data.id");
        String createdAt = JsonPath.read(response, "$.data.createdAt");
        assertTrue(createdAt.endsWith("Z"));
        assertTrue(Instant.parse(createdAt).isAfter(before));
        assertEquals(createdAt, JsonPath.read(response, "$.data.updatedAt"));
        jdbc.queryForObject("""
                SELECT created_by, updated_by, created_at, updated_at, is_deleted
                FROM product_category WHERE id = ?
                """, (rs, rowNum) -> {
            assertEquals(42L, rs.getLong("created_by"));
            assertEquals(42L, rs.getLong("updated_by"));
            assertEquals(createdAt, rs.getString("created_at"));
            assertEquals(createdAt, rs.getString("updated_at"));
            assertEquals(0, rs.getInt("is_deleted"));
            return true;
        }, id.longValue());

        create("""
                {"parendId":%d,"categoryCode":"temperature","categoryName":"Temperature","description":"Thermometers"}
                """.formatted(id.longValue()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.parentId").value(id.intValue()));
    }

    @Test
    void rejectsInvalidInputAndUnauthenticatedRequests() throws Exception {
        mvc.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content(CATEGORY))
                .andExpect(status().isUnauthorized());
        create("""
                {"categoryCode":"invalid code","categoryName":"","description":"Example"}
                """).andExpect(status().isBadRequest());
        create("""
                {"categoryCode":"sensor","categoryName":"Sensors"}
                """).andExpect(status().isBadRequest());
        create("""
                {"parendId":-1,"categoryCode":"sensor","categoryName":"Sensors","description":"Example"}
                """).andExpect(status().isBadRequest());
        create("""
                {"parendId":999999,"categoryCode":"sensor","categoryName":"Sensors","description":"Example"}
                """).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRODUCT_CATEGORY_PARENT_INVALID"))
                .andExpect(jsonPath("$.msg").value("Parent category not found"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM product_category", Integer.class));
    }

    @Test
    void rejectsDuplicateCodesEvenAfterLogicalDeletion() throws Exception {
        create(CATEGORY).andExpect(status().isCreated());
        create(CATEGORY).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_CATEGORY_CODE_EXISTS"))
                .andExpect(jsonPath("$.msg").value("Category code already exists"));

        jdbc.update("UPDATE product_category SET is_deleted = 1 WHERE category_code = 'sensor'");
        create(CATEGORY).andExpect(status().isConflict());
        assertThrows(DataAccessException.class, () -> jdbc.update(
                "INSERT INTO product_category (category_code, category_name) VALUES (?, ?)", "sensor", "Duplicate"));
    }

    private ResultActions create(String body) throws Exception {
        return mvc.perform(post(PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
