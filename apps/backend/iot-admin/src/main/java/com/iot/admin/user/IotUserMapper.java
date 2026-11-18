package com.iot.admin.user;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IotUserMapper extends BaseMapper<IotUser> {
    @Select("SELECT * FROM iot_user ORDER BY id LIMIT #{limit} OFFSET #{offset}")
    List<IotUser> page(@Param("limit") int limit, @Param("offset") long offset);

    @Select("SELECT * FROM iot_user WHERE LOWER(email) = #{email}")
    IotUser findByEmail(@Param("email") String email);
}
