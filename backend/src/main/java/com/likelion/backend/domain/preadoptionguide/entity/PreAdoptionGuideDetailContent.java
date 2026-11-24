package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 세부 안내의 본문 콘텐츠
@Entity
@Table(name = "pre_adoption_guide_detail_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideDetailContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private PreAdoptionGuideDetail detail;

    @Column(name = "content_title", length = 100)
    private String contentTitle;

    @Column(name = "badge", length = 50)
    private String badge;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "content_order", nullable = false)
    private Integer contentOrder;

    @Builder
    public PreAdoptionGuideDetailContent(
            PreAdoptionGuideDetail detail,
            String contentTitle,
            String badge,
            String contentText,
            Integer contentOrder
    ) {
        this.detail = detail;
        this.contentTitle = contentTitle;
        this.badge = badge;
        this.contentText = contentText;
        this.contentOrder = contentOrder;
    }
}
