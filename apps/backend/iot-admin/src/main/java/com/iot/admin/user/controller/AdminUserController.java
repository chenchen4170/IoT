package com.iot.admin.user.controller;

import java.util.List;

import com.iot.admin.user.dto.StatusRequest;
import com.iot.admin.user.dto.UserResponse;
import com.iot.admin.user.service.AuthService;
import com.iot.admin.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AuthService auth;
    private final UserService users;

    public AdminUserController(AuthService auth, UserService users) {
        this.auth = auth;
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                   @RequestParam(defaultValue = "1") @Min(1) int page,
                                   @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        auth.requireAdmin(authorization);
        return users.list(page, size);
    }

    @GetMapping("/{id}")
    public UserResponse get(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @PathVariable Long id) {
        auth.requireAdmin(authorization);
        return users.get(id);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        auth.requireAdmin(authorization);
        return users.updateStatus(id, request.status());
    }
}
