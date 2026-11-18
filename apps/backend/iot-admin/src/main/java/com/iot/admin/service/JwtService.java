package com.iot.admin.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration ttl;

    public JwtService(@Value("${auth.jwt-secret:}") String configuredSecret,
                      @Value("${auth.access-token-ttl}") Duration ttl) {
        byte[] key;
        if (configuredSecret.isBlank()) {
            key = new byte[32];
            new SecureRandom().nextBytes(key);
            log.warn("AUTH_JWT_SECRET is not configured; access tokens will be invalid after restart");
        } else {
            try {
                key = Base64.getDecoder().decode(configuredSecret);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("AUTH_JWT_SECRET must be base64-encoded", exception);
            }
            if (key.length < 32) {
                throw new IllegalArgumentException("AUTH_JWT_SECRET must contain at least 32 bytes");
            }
        }
        SecretKey secretKey = new SecretKeySpec(key, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        this.ttl = ttl;
    }

    public String issue(Long userId, String username, String sessionId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("sid", sessionId)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .id(UUID.randomUUID().toString())
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
