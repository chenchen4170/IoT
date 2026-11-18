package com.iot.admin.dto;

import com.iot.admin.entity.IotUser;

public record RegisterResponse(Long id, String username, String createAt, String updateAt) {

    public static RegisterResponse from(IotUser user) {
        return new RegisterResponse(user.getId(), user.getUsername(), user.getCreateAt(), user.getUpdateAt());
    }
}
