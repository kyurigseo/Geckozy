package com.likelion.backend.domain.auth.service;

import com.likelion.backend.domain.auth.dto.request.LoginRequest;
import com.likelion.backend.domain.auth.dto.request.SignupRequest;
import com.likelion.backend.domain.auth.jwt.JwtProvider;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(SignupRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email(),
                encodedPassword
        );

        User savedUser = userRepository.save(user);

        return savedUser.getUserId();
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_CREDENTIALS)
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return jwtProvider.generateToken(
                user.getUserId(),
                user.getUsername()
        );
    }
}