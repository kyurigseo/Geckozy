package com.likelion.backend.domain.preadoptionguide.entity;

import com.likelion.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 사용자별 체크 항목 선택 상태
@Entity
@Table(
        name = "pre_adoption_guide_user_checks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pre_guide_user_check",
                columnNames = {"user_id", "check_item_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuideUserCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_check_id")
    private Long userCheckId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_item_id", nullable = false)
    private PreAdoptionGuideCheckItem checkItem;

    @Column(name = "checked", nullable = false)
    private boolean checked;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PreAdoptionGuideUserCheck(User user, PreAdoptionGuideCheckItem checkItem, boolean checked) {
        this.user = user;
        this.checkItem = checkItem;
        this.checked = checked;
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
