package com.iot.admin.controller;

import com.iot.admin.dto.LoginRequest;
import com.iot.admin.dto.LoginResponse;
import com.iot.admin.dto.RegisterRequest;
import com.iot.admin.dto.RegisterResponse;
import com.iot.admin.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
