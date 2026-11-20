package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 단계별 체크 항목
@Entity
@Table(name = "pre_adoption_guide_check_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideCheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_item_id")
    private Long checkItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private PreAdoptionGuideStep step;

    @Column(name = "check_title", nullable = false, length = 200)
    private String checkTitle;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    // 단계 완료 시 반드시 체크해야 하는 항목인지
    @Column(name = "required", nullable = false)
    private boolean required;

    @Builder
    public PreAdoptionGuideCheckItem(
            PreAdoptionGuideStep step,
            String checkTitle,
            Integer itemOrder,
            boolean required
    ) {
        this.step = step;
        this.checkTitle = checkTitle;
        this.itemOrder = itemOrder;
        this.required = required;
    }
}
