package com.iot.admin.product.category.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.iot.admin.product.category.entity.ProductCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCategoryResponseTests {

    @Test
    void convertsTimestampsToShanghaiLocalTime() {
        ProductCategory category = new ProductCategory();
        category.setCreatedAt(OffsetDateTime.parse("2026-11-17T20:30:00Z"));
        category.setUpdatedAt(OffsetDateTime.parse("2026-11-18T04:00:00+09:00"));

        ProductCategoryResponse response = ProductCategoryResponse.from(category);

        assertEquals(LocalDateTime.parse("2026-11-18T04:30:00"), response.createdAt());
        assertEquals(LocalDateTime.parse("2026-11-18T03:00:00"), response.updatedAt());
    }
}
