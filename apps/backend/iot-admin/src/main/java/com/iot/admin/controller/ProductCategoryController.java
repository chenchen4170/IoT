package com.iot.admin.controller;

import com.iot.admin.dto.productcategory.ProductCategoryRequest;
import com.iot.admin.dto.productcategory.ProductCategoryResponse;
import com.iot.admin.service.ProductCategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/product-category")
public class ProductCategoryController {

    @Resource
    private ProductCategoryService productCategoryService;

    @PostMapping
    public ResponseEntity<ProductCategoryResponse> create(
            @Valid @RequestBody ProductCategoryRequest request,
            @RequestAttribute("authenticatedUserId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productCategoryService.create(request, userId));
    }
}
