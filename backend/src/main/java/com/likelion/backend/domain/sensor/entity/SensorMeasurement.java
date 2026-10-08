package com.likelion.backend.domain.sensor.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_measurements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Builder
    public SensorMeasurement(Sensor sensor, BigDecimal temperature, BigDecimal humidity, LocalDateTime measuredAt) {
        this.sensor = sensor;
        this.temperature = temperature;
        this.humidity = humidity;
        this.measuredAt = measuredAt != null ? measuredAt : LocalDateTime.now();
    }
}