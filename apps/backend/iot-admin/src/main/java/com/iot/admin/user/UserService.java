package com.iot.admin.user;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final IotUserMapper mapper;

    public UserService(IotUserMapper mapper) {
        this.mapper = mapper;
    }

    public IotUser findByEmail(String email) {
        return mapper.findByEmail(email);
    }

    public IotUser requireActive(Long id) {
        IotUser user = mapper.selectById(id);
        if (user == null || user.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return user;
    }

    public UserResponse get(Long id) {
        IotUser user = mapper.selectById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return UserResponse.from(user);
    }

    public List<UserResponse> list(int page, int size) {
        return mapper.page(size, ((long) page - 1) * size).stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse register(String email, String passwordHash, String displayName) {
        IotUser user = new IotUser();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setDisplayName(displayName);
        user.setStatus(1);
        try {
            mapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        return get(user.getId());
    }

    public void recordLogin(Long id) {
        mapper.update(null, new UpdateWrapper<IotUser>().eq("id", id).setSql("last_login_at = CURRENT_TIMESTAMP"));
    }

    @Transactional
    public UserResponse updateDisplayName(Long id, String displayName) {
        requireActive(id);
        mapper.update(null, new UpdateWrapper<IotUser>().eq("id", id).set("display_name", displayName));
        return get(id);
    }

    @Transactional
    public UserResponse updateStatus(Long id, int status) {
        get(id);
        mapper.update(null, new UpdateWrapper<IotUser>().eq("id", id).set("status", status));
        return get(id);
    }
}
