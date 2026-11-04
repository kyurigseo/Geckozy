package com.likelion.backend.domain.sensor.repository;

import com.likelion.backend.domain.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByEnclosure_Id(Long enclosureId);
}