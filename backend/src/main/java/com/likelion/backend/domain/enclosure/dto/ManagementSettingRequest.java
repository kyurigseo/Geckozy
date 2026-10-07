package com.likelion.backend.domain.enclosure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.management.entity.ManagementSetting;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ManagementSettingRequest {

    private ManagementSetting.SprayingMethod sprayingMethod;
    private ManagementSetting.SprayingFrequency sprayingFrequency;
    private List<ManagementSetting.SprayingTime> sprayingTimes;

    private Boolean lightingEnabled;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightingStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightingEndTime;

    private ManagementSetting.HeatingUsageMode heatingUsageMode;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime heatingStartTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime heatingEndTime;

    public ManagementSetting toEntity(Enclosure enclosure) {
        return ManagementSetting.builder()
                .enclosure(enclosure)
                .sprayingMethod(sprayingMethod)
                .sprayingFrequency(sprayingFrequency)
                .lightingEnabled(lightingEnabled)
                .lightingStartTime(lightingStartTime)
                .lightingEndTime(lightingEndTime)
                .heatingUsageMode(heatingUsageMode)
                .heatingStartTime(heatingStartTime)
                .heatingEndTime(heatingEndTime)
                .build();
    }
}