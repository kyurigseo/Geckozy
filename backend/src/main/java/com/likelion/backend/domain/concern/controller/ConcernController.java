package com.likelion.backend.domain.concern.controller;

import com.likelion.backend.domain.concern.dto.ConcernResponse;
import com.likelion.backend.domain.concern.service.ConcernService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/concerns")
@RequiredArgsConstructor
public class ConcernController {

    private final ConcernService concernService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<ConcernResponse>>>> getConcerns() {
        List<ConcernResponse> concerns = concernService.getAllConcerns();
        Map<String, List<ConcernResponse>> data = Map.of("concerns", concerns);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}