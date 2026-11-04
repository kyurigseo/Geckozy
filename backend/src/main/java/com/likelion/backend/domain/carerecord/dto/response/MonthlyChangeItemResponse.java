package com.likelion.backend.domain.carerecord.dto.response;

import lombok.Getter;

@Getter
public class MonthlyChangeItemResponse {

    private final String type;
    private final int previousCount;
    private final int currentCount;
    private final int changeValue;
    private final String changeStatus;

    public MonthlyChangeItemResponse(
            String type,
            int previousCount,
            int currentCount
    ) {
        this.type = type;
        this.previousCount = previousCount;
        this.currentCount = currentCount;
        this.changeValue = currentCount - previousCount;

        if (changeValue > 0) {
            this.changeStatus = "INCREASED";
        } else if (changeValue < 0) {
            this.changeStatus = "DECREASED";
        } else {
            this.changeStatus = "UNCHANGED";
        }
    }
}