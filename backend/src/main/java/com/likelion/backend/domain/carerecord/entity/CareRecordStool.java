package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.StoolStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "care_record_stools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordStool {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "stool_status", nullable = false)
    private StoolStatus stoolStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stool_abnormalities", columnDefinition = "json")
    private List<String> stoolAbnormalities;

    public CareRecordStool(
            CareRecord careRecord,
            StoolStatus stoolStatus,
            List<String> stoolAbnormalities
    ) {
        this.careRecord = careRecord;
        this.stoolStatus = stoolStatus;
        this.stoolAbnormalities = stoolAbnormalities;
    }
}