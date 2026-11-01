package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.AbnormalBehaviorType;
import com.likelion.backend.domain.carerecord.entity.enums.AbnormalCurrentStatus;
import com.likelion.backend.domain.carerecord.entity.enums.AbnormalOnset;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "care_record_abnormal_behaviors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordAbnormalBehavior {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_behavior_type", nullable = false)
    private AbnormalBehaviorType abnormalBehaviorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_onset", nullable = false)
    private AbnormalOnset abnormalOnset;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_current_status", nullable = false)
    private AbnormalCurrentStatus abnormalCurrentStatus;

    public CareRecordAbnormalBehavior(
            CareRecord careRecord,
            AbnormalBehaviorType abnormalBehaviorType,
            AbnormalOnset abnormalOnset,
            AbnormalCurrentStatus abnormalCurrentStatus
    ) {
        this.careRecord = careRecord;
        this.abnormalBehaviorType = abnormalBehaviorType;
        this.abnormalOnset = abnormalOnset;
        this.abnormalCurrentStatus = abnormalCurrentStatus;
    }
}