package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.controller.PreAdoptionGuideController;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideResponse;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.global.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreAdoptionGuideControllerTest {

    @Mock
    private PreAdoptionGuideService preAdoptionGuideService;

    @InjectMocks
    private PreAdoptionGuideController preAdoptionGuideController;

    @Test
    @DisplayName("1. 해당 종 사전 입양 가이드 조회 응답 포맷 검증")
    void getGuideBySpecies_ReturnsSuccessResponse() {
        PreAdoptionGuideResponse mockResponse =
                new PreAdoptionGuideResponse(1L, 1L, "크레스티드 게코 입양 준비", "입양 전 사육 환경을 준비해요.");

        when(preAdoptionGuideService.getGuideBySpecies(1L)).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<PreAdoptionGuideResponse>> response =
                preAdoptionGuideController.getGuideBySpecies(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().getPreGuideId()).isEqualTo(1L);
        assertThat(response.getBody().getData().getTitle()).isEqualTo("크레스티드 게코 입양 준비");

        verify(preAdoptionGuideService).getGuideBySpecies(1L);
    }

    @Test
    @DisplayName("2. 사전 입양 가이드 진행 상태 조회 응답 포맷 검증")
    void getProgress_ReturnsSuccessResponse() {
        PreAdoptionGuideProgressResponse mockResponse =
                new PreAdoptionGuideProgressResponse(1L, 1L, 1, 0);

        when(preAdoptionGuideService.getProgress(10L, 1L)).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<PreAdoptionGuideProgressResponse>> response =
                preAdoptionGuideController.getProgress(10L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().getProgressId()).isEqualTo(1L);
        assertThat(response.getBody().getData().getPreGuideId()).isEqualTo(1L);
        assertThat(response.getBody().getData().getCurrentStep()).isEqualTo(1);
        assertThat(response.getBody().getData().getCompletedStep()).isEqualTo(0);

        verify(preAdoptionGuideService).getProgress(10L, 1L);
    }
}
