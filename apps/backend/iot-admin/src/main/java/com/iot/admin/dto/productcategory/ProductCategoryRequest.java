package com.iot.admin.dto.productcategory;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductCategoryRequest(
        @JsonAlias("parentId") @Min(0) Long parentId,
        @NotBlank @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String categoryCode,
        @NotBlank @Size(max = 100) String categoryName,
        @NotBlank @Size(max = 500) String description) {
}
