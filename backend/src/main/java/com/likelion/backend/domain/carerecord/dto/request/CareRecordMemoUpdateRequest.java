package com.likelion.backend.domain.carerecord.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CareRecordMemoUpdateRequest {

    @Size(max = 500)
    private String memo;
}