package com.iot.admin.user.dto;

import java.time.OffsetDateTime;

import com.iot.admin.user.entity.IotUser;
public record UserResponse(Long id, String email, String displayName, int status,
                           OffsetDateTime emailVerifiedAt, OffsetDateTime lastLoginAt,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static UserResponse from(IotUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getStatus(), user.getEmailVerifiedAt(), user.getLastLoginAt(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
