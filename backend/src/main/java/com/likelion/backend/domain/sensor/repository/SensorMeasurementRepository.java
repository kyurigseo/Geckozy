package com.likelion.backend.domain.sensor.repository;

import com.likelion.backend.domain.sensor.entity.Sensor;
import com.likelion.backend.domain.sensor.entity.SensorMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorMeasurementRepository extends JpaRepository<SensorMeasurement, Long> {
    // 최신 측정 온도 1건 조회
    Optional<SensorMeasurement> findTopBySensor_SensorIdOrderByMeasuredAtDesc(Long sensorId);

    List<SensorMeasurement> findTop10BySensor_SensorIdOrderByMeasuredAtDesc(Long sensorId);

    List<SensorMeasurement> findBySensor_SensorIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long sensorId, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("delete from SensorMeasurement m where m.sensor = :sensor")
    void deleteBySensor(@Param("sensor") Sensor sensor);
}