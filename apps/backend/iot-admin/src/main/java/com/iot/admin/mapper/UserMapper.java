package com.iot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.admin.entity.IotUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<IotUser> {
}
