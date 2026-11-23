package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.FeedingResponse;
import com.likelion.backend.domain.carerecord.entity.enums.FeedingType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "care_record_feedings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordFeeding {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "feeding_type", nullable = false)
    private FeedingType feedingType;

    @Column(name = "feeding_custom_name", length = 100)
    private String feedingCustomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "feeding_response", nullable = false)
    private FeedingResponse feedingResponse;

    public CareRecordFeeding(
            CareRecord careRecord,
            FeedingType feedingType,
            String feedingCustomName,
            FeedingResponse feedingResponse
    ) {
        this.careRecord = careRecord;
        this.feedingType = feedingType;
        this.feedingCustomName = feedingCustomName;
        this.feedingResponse = feedingResponse;
    }
}