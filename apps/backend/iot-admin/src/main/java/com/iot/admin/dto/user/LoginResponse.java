package com.iot.admin.dto.user;

public record LoginResponse(Long id, String username, String token, String refreshToken) {
}
