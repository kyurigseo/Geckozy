package com.likelion.backend.domain.guidebook;

import com.likelion.backend.domain.guidebook.dto.GuidebookPageResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapListResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapResponse;
import com.likelion.backend.domain.guidebook.service.GuidebookService;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuidebookServiceTest {

    private GuidebookService guidebookService;

    @BeforeEach
    void setUp() {
        guidebookService = new GuidebookService();
    }

    @Test
    @DisplayName("1. 관리 도감 콘텐츠 제공 - 정상 조회 시 페이지 정보 및 콘텐츠 반환")
    void getGuidebookPage_Success() {
        Long guidebookId = 1L;
        int page = 1;

        GuidebookPageResponse response = guidebookService.getGuidebookPage(guidebookId, page);

        assertThat(response).isNotNull();
        assertThat(response.getGuidebookId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("사육장 환기 관리");
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getContent()).isNotNull();
        assertThat(response.getContent().getTitle()).isEqualTo("환기가 필요한 이유");
        assertThat(response.isHasPrevious()).isFalse();
        assertThat(response.isHasNext()).isTrue();
    }

    @Test
    @DisplayName("1. 관리 도감 콘텐츠 제공 - 존재하지 않는 도감 조회 시 예외 발생")
    void getGuidebookPage_NotFound() {
        assertThatThrownBy(() -> guidebookService.getGuidebookPage(999L, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIDEBOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("1. 관리 도감 콘텐츠 제공 - 유효하지 않은 페이지 번호(0 이하) 시 예외 발생")
    void getGuidebookPage_InvalidPage() {
        assertThatThrownBy(() -> guidebookService.getGuidebookPage(1L, 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGE);
    }

    @Test
    @DisplayName("1. 관리 도감 콘텐츠 제공 - 최대 페이지 초과 시 예외 발생")
    void getGuidebookPage_PageNotFound() {
        assertThatThrownBy(() -> guidebookService.getGuidebookPage(1L, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIDEBOOK_PAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("2. 관리 도감 스크랩 - 정상 스크랩 시 스크랩 정보 반환")
    void scrapGuidebook_Success() {
        Long userId = 1L;
        Long guidebookId = 1L;

        GuidebookScrapResponse response = guidebookService.scrapGuidebook(userId, guidebookId);

        assertThat(response).isNotNull();
        assertThat(response.getGuidebookId()).isEqualTo(guidebookId);
        assertThat(response.isScrapped()).isTrue();
        assertThat(response.getScrappedAt()).isNotBlank();
    }

    @Test
    @DisplayName("2. 관리 도감 스크랩 - 비로그인(userId null) 시 UNAUTHORIZED 예외 발생")
    void scrapGuidebook_Unauthorized() {
        assertThatThrownBy(() -> guidebookService.scrapGuidebook(null, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("2. 관리 도감 스크랩 - 존재하지 않는 도감 스크랩 시 예외 발생")
    void scrapGuidebook_NotFound() {
        assertThatThrownBy(() -> guidebookService.scrapGuidebook(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIDEBOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("2. 관리 도감 스크랩 - 중복 스크랩 시 ALREADY_SCRAPPED 예외 발생")
    void scrapGuidebook_AlreadyScrapped() {
        Long userId = 1L;
        Long guidebookId = 1L;

        guidebookService.scrapGuidebook(userId, guidebookId);

        assertThatThrownBy(() -> guidebookService.scrapGuidebook(userId, guidebookId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_SCRAPPED);
    }

    @Test
    @DisplayName("3. 관리 도감 스크랩 해제 - 정상 해제 시 scrapped false 반환")
    void unscrapGuidebook_Success() {
        Long userId = 1L;
        Long guidebookId = 1L;

        guidebookService.scrapGuidebook(userId, guidebookId);
        GuidebookScrapResponse response = guidebookService.unscrapGuidebook(userId, guidebookId);

        assertThat(response).isNotNull();
        assertThat(response.getGuidebookId()).isEqualTo(guidebookId);
        assertThat(response.isScrapped()).isFalse();
    }

    @Test
    @DisplayName("3. 관리 도감 스크랩 해제 - 비로그인(userId null) 시 UNAUTHORIZED 예외 발생")
    void unscrapGuidebook_Unauthorized() {
        assertThatThrownBy(() -> guidebookService.unscrapGuidebook(null, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("3. 관리 도감 스크랩 해제 - 존재하지 않는 도감 시 GUIDEBOOK_NOT_FOUND 예외 발생")
    void unscrapGuidebook_GuidebookNotFound() {
        assertThatThrownBy(() -> guidebookService.unscrapGuidebook(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GUIDEBOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("3. 관리 도감 스크랩 해제 - 스크랩하지 않은 도감 해제 시 SCRAP_NOT_FOUND 예외 발생")
    void unscrapGuidebook_ScrapNotFound() {
        assertThatThrownBy(() -> guidebookService.unscrapGuidebook(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SCRAP_NOT_FOUND);
    }

    @Test
    @DisplayName("4. 스크랩한 관리 도감 조회 - 스크랩 목록 정상 조회")
    void getScrappedGuidebooks_Success() {
        Long userId = 1L;
        guidebookService.scrapGuidebook(userId, 1L);
        guidebookService.scrapGuidebook(userId, 2L);

        GuidebookScrapListResponse response = guidebookService.getScrappedGuidebooks(userId);

        assertThat(response).isNotNull();
        assertThat(response.getGuidebooks()).hasSize(2);
        assertThat(response.getGuidebooks().get(0).getTitle()).isEqualTo("사육장 환기 관리");
        assertThat(response.getGuidebooks().get(0).getThumbnailUrl()).isEqualTo("/images/guidebooks/1/thumbnail.png");
        assertThat(response.getGuidebooks().get(0).getScrappedAt()).isNotBlank();
    }

    @Test
    @DisplayName("4. 스크랩한 관리 도감 조회 - 스크랩 내역이 없을 때 빈 배열 반환")
    void getScrappedGuidebooks_Empty() {
        Long userId = 1L;

        GuidebookScrapListResponse response = guidebookService.getScrappedGuidebooks(userId);

        assertThat(response).isNotNull();
        assertThat(response.getGuidebooks()).isNotNull();
        assertThat(response.getGuidebooks()).isEmpty();
    }

    @Test
    @DisplayName("4. 스크랩한 관리 도감 조회 - 비로그인(userId null) 시 UNAUTHORIZED 예외 발생")
    void getScrappedGuidebooks_Unauthorized() {
        assertThatThrownBy(() -> guidebookService.getScrappedGuidebooks(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}
