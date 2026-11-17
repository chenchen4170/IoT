package com.iot.admin.product.category.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.admin.product.category.dto.ProductCategoryRequest;
import com.iot.admin.product.category.dto.ProductCategoryResponse;
import com.iot.admin.product.category.entity.ProductCategory;
import com.iot.admin.product.category.mapper.ProductCategoryMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductCategoryService {

    private final ProductCategoryMapper mapper;

    public ProductCategoryService(ProductCategoryMapper mapper) {
        this.mapper = mapper;
    }

    public List<ProductCategoryResponse> list() {
        return mapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                        .orderByAsc(ProductCategory::getParentId, ProductCategory::getSortOrder, ProductCategory::getId))
                .stream().map(ProductCategoryResponse::from).toList();
    }

    public ProductCategoryResponse get(Long id) {
        return ProductCategoryResponse.from(requireCategory(id));
    }

    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        validateParent(request.parentId(), null);
        ProductCategory category = new ProductCategory();
        apply(request, category);
        mapper.insert(category);
        return get(category.getId());
    }

    @Transactional
    public ProductCategoryResponse update(Long id, ProductCategoryRequest request) {
        ProductCategory category = requireCategory(id);
        validateParent(request.parentId(), id);
        apply(request, category);
        mapper.updateById(category);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        requireCategory(id);
        Long children = mapper.selectCount(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (children > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delete child categories first");
        }
        mapper.deleteById(id);
    }

    private ProductCategory requireCategory(Long id) {
        ProductCategory category = mapper.selectById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product category not found");
        }
        return category;
    }

    private void validateParent(Long parentId, Long currentId) {
        Long ancestorId = parentId == null ? 0L : parentId;
        Set<Long> visited = new HashSet<>();
        while (ancestorId != 0L) {
            if (ancestorId.equals(currentId) || !visited.add(ancestorId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category hierarchy has a cycle");
            }
            ancestorId = requireCategory(ancestorId).getParentId();
        }
    }

    private void apply(ProductCategoryRequest request, ProductCategory category) {
        category.setParentId(request.parentId() == null ? 0L : request.parentId());
        category.setCategoryCode(request.categoryCode());
        category.setCategoryName(request.categoryName());
        category.setDescription(request.description());
        category.setIconUrl(request.iconUrl());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status() == null ? 1 : request.status());
    }
}
