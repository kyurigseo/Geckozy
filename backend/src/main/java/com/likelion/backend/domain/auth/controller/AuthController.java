package com.likelion.backend.domain.auth.controller;

import com.likelion.backend.domain.auth.dto.request.LoginRequest;
import com.likelion.backend.domain.auth.dto.response.LoginResponse;
import com.likelion.backend.domain.auth.dto.request.SignupRequest;
import com.likelion.backend.domain.auth.dto.response.UsernameCheckResponse;
import com.likelion.backend.domain.auth.service.AuthService;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import com.likelion.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Long userId = authService.signup(request);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.created(
                        Map.of("userId", userId)
                ));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<UsernameCheckResponse>> checkUsername(
            @RequestParam String username
    ) {
        if (username == null || username.isBlank() || username.length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean available = authService.isUsernameAvailable(username);

        return ResponseEntity.ok(
                ApiResponse.success(
                        new UsernameCheckResponse(available)
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String accessToken = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        new LoginResponse(accessToken)
                )
        );
    }
}