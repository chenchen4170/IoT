package com.iot.admin.product.category.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.iot.admin.product.category.entity.ProductCategory;

public record ProductCategoryResponse(
        Long id,
        Long parentId,
        String categoryCode,
        String categoryName,
        String description,
        String iconUrl,
        Integer sortOrder,
        Integer status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(), category.getParentId(), category.getCategoryCode(),
                category.getCategoryName(), category.getDescription(), category.getIconUrl(),
                category.getSortOrder(), category.getStatus(), category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
