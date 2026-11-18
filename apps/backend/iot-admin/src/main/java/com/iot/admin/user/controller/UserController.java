package com.iot.admin.user.controller;

import com.iot.admin.user.dto.ProfileRequest;
import com.iot.admin.user.dto.UserResponse;
import com.iot.admin.user.service.AuthService;
import com.iot.admin.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService auth;
    private final UserService users;

    public UserController(AuthService auth, UserService users) {
        this.auth = auth;
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return UserResponse.from(auth.requireUser(authorization));
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @Valid @RequestBody ProfileRequest request) {
        return users.updateDisplayName(auth.requireUser(authorization).getId(), request.displayName());
    }
}
