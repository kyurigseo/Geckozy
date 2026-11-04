package com.likelion.backend.domain.carerecord.dto.response;

import com.likelion.backend.domain.carerecord.entity.CareRecord;
import com.likelion.backend.domain.carerecord.entity.enums.CareRecordType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CareRecordListResponse {

    private final Long recordId;
    private final CareRecordType recordType;
    private final String memo;
    private final LocalDateTime recordedAt;

    public CareRecordListResponse(CareRecord careRecord) {
        this.recordId = careRecord.getRecordId();
        this.recordType = careRecord.getRecordType();
        this.memo = careRecord.getMemo();
        this.recordedAt = careRecord.getRecordedAt();
    }
}