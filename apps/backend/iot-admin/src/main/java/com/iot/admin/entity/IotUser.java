package com.iot.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("iot_user")
public class IotUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("create_at")
    private String createAt;

    @TableField("update_at")
    private String updateAt;
}
