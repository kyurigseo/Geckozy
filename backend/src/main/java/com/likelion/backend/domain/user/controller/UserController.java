package com.likelion.backend.domain.user.controller;

import com.likelion.backend.domain.user.dto.response.UserMeResponse;
import com.likelion.backend.domain.user.service.UserService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe(
            @AuthenticationPrincipal Long userId
    ) {

        UserMeResponse response = userService.getMe(userId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // 계정 삭제
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Long userId
    ) {

        userService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }
}