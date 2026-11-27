package com.likelion.backend.domain.user.service;

import com.likelion.backend.domain.user.dto.response.UserMeResponse;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.entity.UserStatus;
import com.likelion.backend.domain.user.repository.UserRepository;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 내 정보 조회
    public UserMeResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (user.getStatus() != UserStatus.ACTIVE
                || user.getDeletedAt() != null) {

            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        return new UserMeResponse(
                user.getUserId(),
                user.getUsername()
        );
    }

    // 계정 삭제
    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (user.getStatus() == UserStatus.DELETED
                || user.getDeletedAt() != null) {

            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        user.delete();
    }
}