package com.iot.admin.service;

import com.iot.admin.dto.productcategory.ProductCategoryRequest;
import com.iot.admin.dto.productcategory.ProductCategoryResponse;
import com.iot.admin.entity.ProductCategory;
import com.iot.admin.exception.BizException;
import com.iot.admin.exception.ErrorCode;
import com.iot.admin.mapper.ProductCategoryMapper;
import jakarta.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    public ProductCategoryResponse create(ProductCategoryRequest request, Long userId) {
        String code = request.categoryCode();
        checkCodeAvailable(code);

        long parentId = request.parentId() == null ? 0L : request.parentId();
        if (parentId != 0 && productCategoryMapper.selectById(parentId) == null) {
            throw new BizException(ErrorCode.PRODUCT_CATEGORY_PARENT_INVALID);
        }

        String now = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
        ProductCategory category = new ProductCategory();
        category.setParentId(parentId);
        category.setCategoryCode(code);
        category.setCategoryName(request.categoryName().trim());
        category.setDescription(request.description().trim());
        category.setSortOrder(0);
        category.setStatus(1);
        category.setIsDeleted(0);
        category.setCreatedBy(userId);
        category.setUpdatedBy(userId);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        try {
            productCategoryMapper.insert(category);
        } catch (DataAccessException exception) {
            checkCodeAvailable(code);
            throw exception;
        }
        return ProductCategoryResponse.from(category);
    }

    private void checkCodeAvailable(String code) {
        if (productCategoryMapper.countByCodeIncludingDeleted(code) > 0) {
            throw new BizException(ErrorCode.PRODUCT_CATEGORY_CODE_EXISTS);
        }
    }
}
