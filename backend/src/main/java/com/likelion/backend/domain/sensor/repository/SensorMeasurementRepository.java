package com.likelion.backend.domain.sensor.repository;

import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorMeasurementRepository extends JpaRepository<SensorMeasurement, Long> {
    // 최신 측정 온도 1건 조회
    Optional<SensorMeasurement> findTopBySensor_SensorIdOrderByMeasuredAtDesc(Long sensorId);
}