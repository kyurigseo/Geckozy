package com.likelion.backend.domain.preadoptionguide.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사전 입양 가이드의 단계 (1~5단계)
@Entity
@Table(name = "pre_adoption_guide_steps",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pre_guide_step_number",
                columnNames = {"pre_guide_id", "step_number"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_guide_id", nullable = false)
    private PreAdoptionGuide preAdoptionGuide;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public PreAdoptionGuideStep(
            PreAdoptionGuide preAdoptionGuide,
            Integer stepNumber,
            String title,
            String description
    ) {
        this.preAdoptionGuide = preAdoptionGuide;
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
    }
}
