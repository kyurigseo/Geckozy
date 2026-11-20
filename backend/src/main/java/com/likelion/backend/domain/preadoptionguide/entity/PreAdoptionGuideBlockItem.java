package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 강조 블록 안의 항목
@Entity
@Table(name = "pre_adoption_guide_block_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideBlockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_item_id")
    private Long blockItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private PreAdoptionGuideDetailBlock block;

    @Column(name = "item_content", columnDefinition = "TEXT")
    private String itemContent;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Builder
    public PreAdoptionGuideBlockItem(
            PreAdoptionGuideDetailBlock block,
            String itemContent,
            Integer itemOrder
    ) {
        this.block = block;
        this.itemContent = itemContent;
        this.itemOrder = itemOrder;
    }
}
