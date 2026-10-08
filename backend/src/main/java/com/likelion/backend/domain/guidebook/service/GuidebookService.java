package com.likelion.backend.domain.guidebook.service;

import com.likelion.backend.domain.guidebook.dto.GuidebookPageResponse;
import com.likelion.backend.domain.guidebook.dto.GuidebookScrapResponse;
import com.likelion.backend.global.exception.BusinessException;
import com.likelion.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuidebookService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private static final Map<Long, MockGuidebook> MOCK_GUIDEBOOKS = Map.of(
            1L, new MockGuidebook("사육장 환기 관리", "/images/guidebooks/1/thumbnail.png", List.of(
                    new MockPage("환기가 필요한 이유", "사육장 내부의 공기 흐름을 확인해 주세요.", "/images/guidebooks/1/1.png"),
                    new MockPage("올바른 환기 방법", "상단 통풍구와 하단 통풍구를 동시에 열어 자연 대류를 만들어 주세요.", "/images/guidebooks/1/2.png"),
                    new MockPage("환기 시 주의사항", "직접적인 외풍이 개체에게 닿지 않도록 통풍구 방향을 조절해 주세요.", "/images/guidebooks/1/3.png")
            )),
            2L, new MockGuidebook("습도 관리 가이드", "/images/guidebooks/2/thumbnail.png", List.of(
                    new MockPage("적정 습도 유지의 중요성", "종별 권장 습도 범위를 확인하고 사육장 환경을 점검해 주세요.", "/images/guidebooks/2/1.png"),
                    new MockPage("분무 방법과 타이밍", "저녁 시간대에 미온수로 벽면과 구조물에 분무해 주세요.", "/images/guidebooks/2/2.png"),
                    new MockPage("과습 방지 요령", "주간에는 충분히 건조시켜 곰팡이 발생을 예방해 주세요.", "/images/guidebooks/2/3.png")
            ))
    );

    private final Map<Long, Map<Long, ScrapInfo>> userScraps = new ConcurrentHashMap<>();

    public GuidebookPageResponse getGuidebookPage(Long guidebookId, int page) {
        MockGuidebook guidebook = MOCK_GUIDEBOOKS.get(guidebookId);
        if (guidebook == null) {
            throw new BusinessException(ErrorCode.GUIDEBOOK_NOT_FOUND);
        }

        int totalPages = guidebook.pages.size();

        if (page < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE);
        }
        if (page > totalPages) {
            throw new BusinessException(ErrorCode.GUIDEBOOK_PAGE_NOT_FOUND);
        }

        MockPage mockPage = guidebook.pages.get(page - 1);

        return GuidebookPageResponse.builder()
                .guidebookId(guidebookId)
                .title(guidebook.title)
                .currentPage(page)
                .totalPages(totalPages)
                .content(GuidebookPageResponse.PageContent.builder()
                        .title(mockPage.title)
                        .description(mockPage.description)
                        .imageUrl(mockPage.imageUrl)
                        .build())
                .hasPrevious(page > 1)
                .hasNext(page < totalPages)
                .build();
    }

    public GuidebookScrapResponse scrapGuidebook(Long userId, Long guidebookId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!MOCK_GUIDEBOOKS.containsKey(guidebookId)) {
            throw new BusinessException(ErrorCode.GUIDEBOOK_NOT_FOUND);
        }

        Map<Long, ScrapInfo> scraps = userScraps.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        if (scraps.containsKey(guidebookId)) {
            throw new BusinessException(ErrorCode.ALREADY_SCRAPPED);
        }

        String scrappedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(FORMATTER);
        scraps.put(guidebookId, new ScrapInfo(guidebookId, scrappedAt));

        return GuidebookScrapResponse.builder()
                .guidebookId(guidebookId)
                .scrapped(true)
                .scrappedAt(scrappedAt)
                .build();
    }

    private record MockGuidebook(String title, String thumbnailUrl, List<MockPage> pages) {}
    private record MockPage(String title, String description, String imageUrl) {}
    private record ScrapInfo(Long guidebookId, String scrappedAt) {}
}
