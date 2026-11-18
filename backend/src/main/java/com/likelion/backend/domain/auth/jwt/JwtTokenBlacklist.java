package com.likelion.backend.domain.auth.jwt;

import com.likelion.backend.domain.auth.entity.RevokedToken;
import com.likelion.backend.domain.auth.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class JwtTokenBlacklist {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void add(
            String token,
            LocalDateTime expiresAt
    ) {
        revokedTokenRepository.deleteByExpiresAtBefore(
                LocalDateTime.now(ZoneOffset.UTC)
        );

        revokedTokenRepository.save(
                new RevokedToken(hashToken(token), expiresAt)
        );
    }

    @Transactional(readOnly = true)
    public boolean contains(String token) {
        return revokedTokenRepository.existsById(
                hashToken(token)
        );
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Failed to hash JWT", e
            );
        }
    }
}