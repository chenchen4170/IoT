package com.iot.admin.user;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
