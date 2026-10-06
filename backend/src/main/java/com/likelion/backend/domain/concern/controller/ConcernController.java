package com.likelion.backend.domain.concern.controller;

import com.likelion.backend.domain.concern.dto.ConcernResponse;
import com.likelion.backend.domain.concern.service.ConcernService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/concerns")
@RequiredArgsConstructor
public class ConcernController {

    private final ConcernService concernService;

    @GetMapping
    public ResponseEntity<List<ConcernResponse>> getConcerns() {
        return ResponseEntity.ok(concernService.getAllConcerns());
    }
}