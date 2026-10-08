package com.likelion.backend.domain.guidebook.controller;

import com.likelion.backend.domain.guidebook.dto.GuidebookPageResponse;
import com.likelion.backend.domain.guidebook.service.GuidebookService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guidebooks")
@RequiredArgsConstructor
public class GuidebookController {

    private final GuidebookService guidebookService;

    @GetMapping("/{guidebookId}/pages/{page}")
    public ResponseEntity<ApiResponse<GuidebookPageResponse>> getGuidebookPage(
            @PathVariable("guidebookId") Long guidebookId,
            @PathVariable("page") int page
    ) {
        GuidebookPageResponse response = guidebookService.getGuidebookPage(guidebookId, page);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
