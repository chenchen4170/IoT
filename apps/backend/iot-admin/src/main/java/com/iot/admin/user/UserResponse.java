package com.iot.admin.user;

import java.time.OffsetDateTime;

public record UserResponse(Long id, String email, String displayName, int status,
                           OffsetDateTime emailVerifiedAt, OffsetDateTime lastLoginAt,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static UserResponse from(IotUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getStatus(), user.getEmailVerifiedAt(), user.getLastLoginAt(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
