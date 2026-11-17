package com.iot.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IotAdminApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void healthUsesCommonResponse() throws Exception {
        mvc.perform(get("/api/admin/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.traceId").value(""))
                .andExpect(jsonPath("$.msg").value(""))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void categoryCrudAndLogicalDelete() throws Exception {
        String category = """
                {"categoryCode":"sensor","categoryName":"Sensors","description":"Old"}
                """;
        String location = mvc.perform(post("/api/admin/product-categories")
                        .contentType(MediaType.APPLICATION_JSON).content(category))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andReturn().getResponse().getHeader("Location");
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value("Sensors"));
        mvc.perform(get("/api/admin/product-categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryCode").value("sensor"));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"sensor","categoryName":"Sensors v2","status":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").doesNotExist())
                .andExpect(jsonPath("$.data.status").value(0));
        Assertions.assertNull(jdbc.queryForObject(
                "SELECT description FROM product_category WHERE id = ?", String.class, id));
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        Assertions.assertEquals(1, jdbc.queryForObject(
                "SELECT is_deleted FROM product_category WHERE id = ?", Integer.class, id));
        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("Product category not found"));
        mvc.perform(get("/api/admin/product-categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void rejectsInvalidHierarchyAndDuplicateCode() throws Exception {
        String parent = mvc.perform(post("/api/admin/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"parent","categoryName":"Parent"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long parentId = Long.parseLong(parent.substring(parent.lastIndexOf('/') + 1));

        mvc.perform(post("/api/admin/product-categories").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"parent","categoryName":"Duplicate"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.msg").value("Category code already exists"));
        mvc.perform(post("/api/admin/product-categories").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"invalid code","categoryName":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        String child = mvc.perform(post("/api/admin/product-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"child","categoryName":"Child","parentId":%d}
                                """.formatted(parentId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        long childId = Long.parseLong(child.substring(child.lastIndexOf('/') + 1));

        mvc.perform(delete(parent)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
        mvc.perform(put(parent).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryCode":"parent","categoryName":"Parent","parentId":%d}
                                """.formatted(childId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        mvc.perform(delete(child)).andExpect(status().isNoContent());
        mvc.perform(delete(parent)).andExpect(status().isNoContent());
    }
}
