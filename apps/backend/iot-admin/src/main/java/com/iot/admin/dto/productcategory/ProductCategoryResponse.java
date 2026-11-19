package com.iot.admin.dto.productcategory;

import com.iot.admin.entity.ProductCategory;

public record ProductCategoryResponse(
        Long id,
        Long parentId,
        String categoryCode,
        String categoryName,
        String description,
        Integer sortOrder,
        Integer status,
        Long createdBy,
        Long updatedBy,
        String createdAt,
        String updatedAt) {

    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(category.getId(), category.getParentId(),
                category.getCategoryCode(), category.getCategoryName(), category.getDescription(),
                category.getSortOrder(), category.getStatus(), category.getCreatedBy(), category.getUpdatedBy(),
                category.getCreatedAt(), category.getUpdatedAt());
    }
}
