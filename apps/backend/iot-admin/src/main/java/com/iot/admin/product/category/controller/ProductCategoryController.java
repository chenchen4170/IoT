package com.iot.admin.product.category.controller;

import java.net.URI;
import java.util.List;

import com.iot.admin.product.category.dto.ProductCategoryRequest;
import com.iot.admin.product.category.dto.ProductCategoryResponse;
import com.iot.admin.product.category.service.ProductCategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/product-categories")
public class ProductCategoryController {

    @Resource
    private ProductCategoryService service;

    @GetMapping
    public List<ProductCategoryResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ProductCategoryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<ProductCategoryResponse> create(@Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/admin/product-categories/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ProductCategoryResponse update(@PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
