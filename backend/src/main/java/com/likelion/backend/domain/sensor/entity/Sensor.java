package com.likelion.backend.domain.sensor.entity;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long sensorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enclosure_id", nullable = false, unique = true)
    private Enclosure enclosure;

    @Column(name = "device_identifier", nullable = false, length = 100)
    private String deviceIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.CONNECTED;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private LocalDateTime connectedAt = LocalDateTime.now();

    public enum Status {
        CONNECTED, DISCONNECTED
    }

    @Builder
    public Sensor(Enclosure enclosure, String deviceIdentifier, Status status) {
        this.enclosure = enclosure;
        this.deviceIdentifier = deviceIdentifier;
        this.status = status != null ? status : Status.CONNECTED;
    }
}