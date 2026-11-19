package com.iot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.admin.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    @Select("SELECT COUNT(*) FROM product_category WHERE category_code = #{categoryCode}")
    long countByCodeIncludingDeleted(@Param("categoryCode") String categoryCode);
}
