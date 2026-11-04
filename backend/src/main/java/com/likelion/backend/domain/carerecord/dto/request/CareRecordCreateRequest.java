package com.likelion.backend.domain.carerecord.dto.request;

import tools.jackson.databind.JsonNode;
import com.likelion.backend.domain.carerecord.entity.enums.CareRecordType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CareRecordCreateRequest {

    @NotNull
    private CareRecordType recordType;

    @NotNull
    private LocalDateTime recordedAt;

    @Size(max = 500)
    private String memo;

    private JsonNode typeDetail;
}