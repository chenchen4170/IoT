package com.iot.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("product_category")
public class ProductCategory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("parent_id")
    private Long parentId;

    @TableField("category_code")
    private String categoryCode;

    @TableField("category_name")
    private String categoryName;

    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer status;

    @TableLogic(value = "0", delval = "1")
    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("created_at")
    private String createdAt;

    @TableField("updated_at")
    private String updatedAt;
}
