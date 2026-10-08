package com.likelion.backend.domain.guidebook.controller;

import com.likelion.backend.domain.guidebook.dto.GuidebookPageResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapListResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapResponse;
import com.likelion.backend.domain.guidebook.service.GuidebookService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/{guidebookId}/scrap")
    public ResponseEntity<ApiResponse<GuidebookScrapResponse>> scrapGuidebook(
            @AuthenticationPrincipal Long userId,
            @PathVariable("guidebookId") Long guidebookId
    ) {
        GuidebookScrapResponse response = guidebookService.scrapGuidebook(userId, guidebookId);
        return ResponseEntity.ok(ApiResponse.success("SCRAP_SUCCESS", response));
    }

    @DeleteMapping("/{guidebookId}/scrap")
    public ResponseEntity<ApiResponse<GuidebookScrapResponse>> unscrapGuidebook(
            @AuthenticationPrincipal Long userId,
            @PathVariable("guidebookId") Long guidebookId
    ) {
        GuidebookScrapResponse response = guidebookService.unscrapGuidebook(userId, guidebookId);
        return ResponseEntity.ok(ApiResponse.success("UNSCRAP_SUCCESS", response));
    }

    @GetMapping("/scraps")
    public ResponseEntity<ApiResponse<GuidebookScrapListResponse>> getScrappedGuidebooks(
            @AuthenticationPrincipal Long userId
    ) {
        GuidebookScrapListResponse response = guidebookService.getScrappedGuidebooks(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
