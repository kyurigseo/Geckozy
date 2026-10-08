package com.likelion.backend.domain.concern.service;

import com.likelion.backend.domain.concern.dto.ConcernResponse;
import com.likelion.backend.domain.concern.repository.ConcernRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcernService {

    private final ConcernRepository concernRepository;

    public List<ConcernResponse> getAllConcerns() {
        return concernRepository.findAll().stream()
                .map(ConcernResponse::from)
                .toList();
    }
}