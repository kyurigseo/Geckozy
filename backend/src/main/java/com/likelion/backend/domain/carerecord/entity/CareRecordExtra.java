package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.ExtraType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "care_record_extras")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordExtra {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_type", nullable = false)
    private ExtraType extraType;

    public CareRecordExtra(CareRecord careRecord, ExtraType extraType) {
        this.careRecord = careRecord;
        this.extraType = extraType;
    }
}