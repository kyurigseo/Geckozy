package com.likelion.backend.domain.carerecord.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class CareRecordCalendarResponse {

    private List<LocalDate> dates;
}