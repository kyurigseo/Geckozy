package com.likelion.backend.domain.carerecord.entity;

import com.likelion.backend.domain.carerecord.entity.enums.CleaningScope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "care_record_cleanings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareRecordCleaning {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private CareRecord careRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "cleaning_scope", nullable = false)
    private CleaningScope cleaningScope;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cleaning_targets", columnDefinition = "json")
    private List<String> cleaningTargets;

    public CareRecordCleaning(
            CareRecord careRecord,
            CleaningScope cleaningScope,
            List<String> cleaningTargets
    ) {
        this.careRecord = careRecord;
        this.cleaningScope = cleaningScope;
        this.cleaningTargets = cleaningTargets;
    }
}