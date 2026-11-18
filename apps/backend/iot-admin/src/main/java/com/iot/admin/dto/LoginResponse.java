package com.iot.admin.dto;

public record LoginResponse(Long id, String username, String token, String refreshToken) {
}
