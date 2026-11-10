package com.likelion.backend.domain.carerecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "care_record_weights")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordWeight {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Column(name = "weight_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightG;

    public CareRecordWeight(CareRecord careRecord, BigDecimal weightG) {
        this.careRecord = careRecord;
        this.weightG = weightG;
    }
}