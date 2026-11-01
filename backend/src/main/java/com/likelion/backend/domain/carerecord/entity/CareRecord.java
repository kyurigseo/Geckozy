package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.CareRecordType;
import com.likelion.backend.domain.lizard.entity.Lizard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lizard_id", nullable = false)
    private Lizard lizard;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private CareRecordType recordType;

    @Column(length = 500)
    private String memo;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CareRecord(
            Lizard lizard,
            CareRecordType recordType,
            String memo,
            LocalDateTime recordedAt
    ) {
        this.lizard = lizard;
        this.recordType = recordType;
        this.memo = memo;
        this.recordedAt = recordedAt;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}