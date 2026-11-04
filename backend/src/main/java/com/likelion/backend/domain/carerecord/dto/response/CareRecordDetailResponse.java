package com.likelion.backend.domain.carerecord.dto.response;

import com.likelion.backend.domain.carerecord.entity.CareRecord;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
public class CareRecordDetailResponse {

    private final Long recordId;
    private final Long lizardId;
    private final String recordType;
    private final LocalDateTime recordedAt;
    private final String memo;
    private final Map<String, Object> typeDetail;
    private final List<CareRecordPhotoResponse> photos;
    private final EnvironmentResponse environment;

    public CareRecordDetailResponse(
            CareRecord record,
            Map<String, Object> typeDetail,
            List<CareRecordPhotoResponse> photos,
            EnvironmentResponse environment
    ) {
        this.recordId = record.getRecordId();
        this.lizardId = record.getLizard().getLizardId();
        this.recordType = record.getRecordType().name();
        this.recordedAt = record.getRecordedAt();
        this.memo = record.getMemo();
        this.typeDetail = typeDetail;
        this.photos = photos;
        this.environment = environment;
    }
}