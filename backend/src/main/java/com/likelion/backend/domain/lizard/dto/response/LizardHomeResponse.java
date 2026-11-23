package com.likelion.backend.domain.lizard.dto.response;

import com.likelion.backend.domain.sensor.entity.Sensor;

public record LizardHomeResponse(

        Long lizardId,
        String name,
        String species,

        EnclosureInfo enclosure,
        SensorInfo sensor,

        TemperatureStandard temperatureStandard,
        Range humidityStandard

) {

    public record EnclosureInfo(
            Long enclosureId
    ) {}

    public record SensorInfo(
            Long sensorId,
            Sensor.Status status
    ) {}

    public record TemperatureStandard(
            Range day,
            Range night
    ) {}

    public record Range(
            Double min,
            Double max
    ) {}
}