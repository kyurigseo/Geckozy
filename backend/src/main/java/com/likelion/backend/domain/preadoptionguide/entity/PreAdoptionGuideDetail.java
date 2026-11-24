package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 안내 카드의 세부 안내 (예: 왜 세로형이 필요한가요?)
@Entity
@Table(name = "pre_adoption_guide_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private PreAdoptionGuideSection section;

    @Column(name = "question", nullable = false, length = 200)
    private String question;

    @Column(name = "detail_order", nullable = false)
    private Integer detailOrder;

    @Builder
    public PreAdoptionGuideDetail(
            PreAdoptionGuideSection section,
            String question,
            Integer detailOrder
    ) {
        this.section = section;
        this.question = question;
        this.detailOrder = detailOrder;
    }
}
