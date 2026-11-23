package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강조 블록 (예: 기관 권고 사항, 설치할 때 확인해주세요).
 * 안내 카드(section)에 바로 붙거나, 세부 안내 팝업(detail) 안에 붙는다. 둘 중 하나만 가진다.
 */
@Entity
@Table(name = "pre_adoption_guide_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long blockId;

    // 안내 카드에 바로 붙는 블록이면 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private PreAdoptionGuideSection section;

    // 세부 안내 팝업 안의 블록이면 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_id")
    private PreAdoptionGuideDetail detail;

    @Column(name = "block_title", nullable = false, length = 100)
    private String blockTitle;

    @Column(name = "block_order", nullable = false)
    private Integer blockOrder;

    private PreAdoptionGuideBlock(
            PreAdoptionGuideSection section,
            PreAdoptionGuideDetail detail,
            String blockTitle,
            Integer blockOrder
    ) {
        this.section = section;
        this.detail = detail;
        this.blockTitle = blockTitle;
        this.blockOrder = blockOrder;
    }

    public static PreAdoptionGuideBlock ofSection(PreAdoptionGuideSection section, String blockTitle, Integer blockOrder) {
        return new PreAdoptionGuideBlock(section, null, blockTitle, blockOrder);
    }

    public static PreAdoptionGuideBlock ofDetail(PreAdoptionGuideDetail detail, String blockTitle, Integer blockOrder) {
        return new PreAdoptionGuideBlock(null, detail, blockTitle, blockOrder);
    }
}
