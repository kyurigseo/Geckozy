package com.likelion.backend.domain.preadoptionguide.entity;

import com.likelion.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pre_adoption_guide_progresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pre_guide_progress_user_guide",
                columnNames = {"user_id", "pre_guide_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideProgress {

    public static final int FIRST_STEP = 1;
    public static final int LAST_STEP = 5;
    public static final int NOT_COMPLETED = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_guide_id", nullable = false)
    private PreAdoptionGuide preAdoptionGuide;

    // 현재 진행 중인 단계 (1부터 시작)
    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    // 완료한 마지막 단계 (아직 완료한 단계가 없으면 0)
    @Column(name = "completed_step", nullable = false)
    private Integer completedStep;

    // 가이드 전체 완료 여부 (입양 전 가이드 완료 API에서 true로 변경)
    @Column(name = "completed", nullable = false)
    private boolean completed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private PreAdoptionGuideProgress(User user, PreAdoptionGuide preAdoptionGuide) {
        this.user = user;
        this.preAdoptionGuide = preAdoptionGuide;
        this.currentStep = FIRST_STEP;
        this.completedStep = NOT_COMPLETED;
        this.completed = false;
    }

    // 첫 단계부터 시작하는 초기 진행 상태 생성
    public static PreAdoptionGuideProgress start(User user, PreAdoptionGuide preAdoptionGuide) {
        return new PreAdoptionGuideProgress(user, preAdoptionGuide);
    }

    // 다음에 완료할 수 있는 단계 (순서대로만 완료)
    public int nextStepToComplete() {
        return completedStep + 1;
    }

    // 단계 완료 처리 후 다음 단계로 이동 (마지막 단계에서는 그대로 머묾)
    public void completeStep(int stepNumber) {
        this.completedStep = stepNumber;
        this.currentStep = Math.min(stepNumber + 1, LAST_STEP);
    }
}
