package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 단계 화면의 안내 카드 (예: 01 사육장 형태)
@Entity
@Table(name = "pre_adoption_guide_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private PreAdoptionGuideStep step;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Builder
    public PreAdoptionGuideSection(
            PreAdoptionGuideStep step,
            String title,
            String content,
            Integer sectionOrder
    ) {
        this.step = step;
        this.title = title;
        this.content = content;
        this.sectionOrder = sectionOrder;
    }
}
