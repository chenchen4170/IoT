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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Asia/Shanghai");

    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(), category.getParentId(), category.getCategoryCode(),
                category.getCategoryName(), category.getDescription(), category.getIconUrl(),
                category.getSortOrder(), category.getStatus(), toLocalTime(category.getCreatedAt()),
                toLocalTime(category.getUpdatedAt()));
    }

    private static LocalDateTime toLocalTime(OffsetDateTime value) {
        return value == null ? null : value.atZoneSameInstant(LOCAL_ZONE).toLocalDateTime();
    }
}
