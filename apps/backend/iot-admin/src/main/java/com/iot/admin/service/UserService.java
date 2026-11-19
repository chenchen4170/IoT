package com.iot.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.admin.dto.user.LoginRequest;
import com.iot.admin.dto.user.LoginResponse;
import com.iot.admin.dto.user.RegisterRequest;
import com.iot.admin.dto.user.RegisterResponse;
import com.iot.admin.dto.user.RefreshTokenRequest;
import com.iot.admin.dto.user.RefreshTokenResponse;
import com.iot.admin.entity.IotUser;
import com.iot.admin.exception.BizException;
import com.iot.admin.exception.ErrorCode;
import com.iot.admin.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Resource
    private UserMapper userMapper;

    @Resource
    private TokenStore tokenStore;

    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        String username = request.username().trim();
        if (findByUsername(username) != null) {
            throw new BizException(ErrorCode.CONFLICT);
        }

        IotUser user = new IotUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        try {
            userMapper.insert(user);
        } catch (DataAccessException exception) {
            if (findByUsername(username) != null) {
                throw new BizException(ErrorCode.CONFLICT);
            }
            throw exception;
        }
        return RegisterResponse.from(userMapper.selectById(user.getId()));
    }

    public LoginResponse login(LoginRequest request) {
        IotUser user = findByUsername(request.username().trim());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        TokenStore.Tokens tokens = tokenStore.issue(user.getId(), user.getUsername());
        return new LoginResponse(user.getId(), user.getUsername(), tokens.access(), tokens.refresh());
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        Long userId = tokenStore.refreshUserId(request.refreshToken());
        IotUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        TokenStore.Tokens tokens = tokenStore.refresh(user.getId(), user.getUsername(), request.refreshToken());
        return new RefreshTokenResponse(tokens.access(), tokens.refresh());
    }

    private IotUser findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<IotUser>().eq(IotUser::getUsername, username));
    }
}
