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

    // 체크 항목이 속한 그룹 (예: 5단계의 사육장 / 생활 준비 / 환경 및 센서). 그룹이 없으면 null
    @Column(name = "group_title", length = 50)
    private String groupTitle;

    @Column(name = "check_title", nullable = false, length = 200)
    private String checkTitle;

    // 체크 항목 아래 보조 설명. 없으면 null
    @Column(name = "check_description", length = 200)
    private String checkDescription;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    // 단계 완료 시 반드시 체크해야 하는 항목인지
    @Column(name = "required", nullable = false)
    private boolean required;

    @Builder
    public PreAdoptionGuideCheckItem(
            PreAdoptionGuideStep step,
            String groupTitle,
            String checkTitle,
            String checkDescription,
            Integer itemOrder,
            boolean required
    ) {
        this.step = step;
        this.groupTitle = groupTitle;
        this.checkTitle = checkTitle;
        this.checkDescription = checkDescription;
        this.itemOrder = itemOrder;
        this.required = required;
    }
}
