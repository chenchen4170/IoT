package com.iot.admin.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import com.iot.admin.user.dto.TokenResponse;
import com.iot.admin.user.dto.UserResponse;
import com.iot.admin.user.entity.IotUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService users;
    private final TokenStore tokens;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

    public AuthService(UserService users, TokenStore tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public UserResponse register(String email, String password, String displayName) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
        }
        return users.register(normalize(email), passwords.encode(password), displayName);
    }

    public TokenResponse login(String email, String password) {
        IotUser user = users.findByEmail(normalize(email));
        if (user == null || user.getStatus() != 1 || !passwords.matches(password, user.getPasswordHash())) {
            throw unauthorized();
        }
        users.recordLogin(user.getId());
        return issue(user.getId());
    }

    public TokenResponse refresh(String refreshToken) {
        String oldKey = fingerprint(refreshToken);
        Long userId = tokens.refreshUser(oldKey);
        if (userId == null) {
            throw unauthorized();
        }
        users.requireActive(userId);
        String nextRefresh = randomToken();
        if (!tokens.rotateRefresh(oldKey, fingerprint(nextRefresh), userId)) {
            throw unauthorized();
        }
        return issueAccess(userId, nextRefresh);
    }

    public IotUser requireUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7 || authorization.length() > 256) {
            throw unauthorized();
        }
        Long id = tokens.accessUser(fingerprint(authorization.substring(7)));
        if (id == null) {
            throw unauthorized();
        }
        return users.requireActive(id);
    }

    public IotUser requireAdmin(String authorization) {
        IotUser user = requireUser(authorization);
        return user;
    }

    private TokenResponse issue(Long userId) {
        String refreshToken = randomToken();
        tokens.saveRefresh(fingerprint(refreshToken), userId);
        return issueAccess(userId, refreshToken);
    }

    private TokenResponse issueAccess(Long userId, String refreshToken) {
        String accessToken = randomToken();
        tokens.saveAccess(fingerprint(accessToken), userId);
        return new TokenResponse(accessToken, refreshToken, "Bearer", tokens.accessExpiresIn());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String fingerprint(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}
