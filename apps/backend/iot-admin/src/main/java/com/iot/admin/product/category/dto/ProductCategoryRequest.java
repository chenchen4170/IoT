package com.iot.admin.product.category.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String categoryCode,
        @NotBlank @Size(max = 100) String categoryName,
        @Min(0) Long parentId,
        @Size(max = 500) String description,
        @Size(max = 500) String iconUrl,
        Integer sortOrder,
        @Min(0) @Max(1) Integer status
) {
}
