package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.SheddingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "care_record_shedding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordShedding {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "shedding_status", nullable = false)
    private SheddingStatus sheddingStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shedding_remaining_parts", columnDefinition = "json")
    private List<String> sheddingRemainingParts;

    public CareRecordShedding(
            CareRecord careRecord,
            SheddingStatus sheddingStatus,
            List<String> sheddingRemainingParts
    ) {
        this.careRecord = careRecord;
        this.sheddingStatus = sheddingStatus;
        this.sheddingRemainingParts = sheddingRemainingParts;
    }
}