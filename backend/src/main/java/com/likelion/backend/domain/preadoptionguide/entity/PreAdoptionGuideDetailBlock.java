package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 세부 안내의 강조 블록 (예: 기관 권고 사항)
@Entity
@Table(name = "pre_adoption_guide_detail_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideDetailBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long blockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id", nullable = false)
    private PreAdoptionGuideDetail detail;

    @Column(name = "block_title", nullable = false, length = 100)
    private String blockTitle;

    @Column(name = "block_order", nullable = false)
    private Integer blockOrder;

    @Builder
    public PreAdoptionGuideDetailBlock(
            PreAdoptionGuideDetail detail,
            String blockTitle,
            Integer blockOrder
    ) {
        this.detail = detail;
        this.blockTitle = blockTitle;
        this.blockOrder = blockOrder;
    }
}
