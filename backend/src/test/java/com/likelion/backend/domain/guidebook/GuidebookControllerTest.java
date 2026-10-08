package com.likelion.backend.domain.guidebook;

import com.likelion.backend.domain.guidebook.controller.GuidebookController;
import com.likelion.backend.domain.guidebook.dto.GuidebookPageResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapListResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapResponse;
import com.likelion.backend.domain.guidebook.service.GuidebookService;
import com.likelion.backend.global.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GuidebookControllerTest {

    private GuidebookService guidebookService;
    private GuidebookController guidebookController;

    @BeforeEach
    void setUp() {
        guidebookService = new GuidebookService();
        guidebookController = new GuidebookController(guidebookService);
    }

    @Test
    @DisplayName("1. 관리 도감 콘텐츠 제공 컨트롤러 응답 포맷 검증")
    void getGuidebookPage_ReturnsSuccessResponse() {
        ResponseEntity<ApiResponse<GuidebookPageResponse>> response =
                guidebookController.getGuidebookPage(1L, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getGuidebookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("2. 관리 도감 스크랩 컨트롤러 응답 포맷 검증")
    void scrapGuidebook_ReturnsScrapSuccessResponse() {
        Long userId = 1L;
        Long guidebookId = 1L;

        ResponseEntity<ApiResponse<GuidebookScrapResponse>> response =
                guidebookController.scrapGuidebook(userId, guidebookId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SCRAP_SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getGuidebookId()).isEqualTo(guidebookId);
        assertThat(response.getBody().getData().isScrapped()).isTrue();
        assertThat(response.getBody().getData().getScrappedAt()).isNotBlank();
    }

    @Test
    @DisplayName("3. 관리 도감 스크랩 해제 컨트롤러 응답 포맷 검증")
    void unscrapGuidebook_ReturnsUnscrapSuccessResponse() {
        Long userId = 1L;
        Long guidebookId = 1L;

        guidebookController.scrapGuidebook(userId, guidebookId);
        ResponseEntity<ApiResponse<GuidebookScrapResponse>> response =
                guidebookController.unscrapGuidebook(userId, guidebookId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("UNSCRAP_SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getGuidebookId()).isEqualTo(guidebookId);
        assertThat(response.getBody().getData().isScrapped()).isFalse();
    }

    @Test
    @DisplayName("4. 스크랩한 관리 도감 조회 컨트롤러 응답 포맷 검증")
    void getScrappedGuidebooks_ReturnsSuccessResponse() {
        Long userId = 1L;
        guidebookController.scrapGuidebook(userId, 1L);

        ResponseEntity<ApiResponse<GuidebookScrapListResponse>> response =
                guidebookController.getScrappedGuidebooks(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getGuidebooks()).hasSize(1);
    }
}
