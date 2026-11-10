package com.likelion.backend.domain.carerecord.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class MonthlyChangesResponse {

    private final List<MonthlyChangeItemResponse> items;
    private final String message;

    public MonthlyChangesResponse(
            List<MonthlyChangeItemResponse> items,
            String message
    ) {
        this.items = items;
        this.message = message;
    }
}