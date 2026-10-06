package com.likelion.backend.domain.management.entity;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "management_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ManagementSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "management_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enclosure_id", nullable = false, unique = true)
    private Enclosure enclosure;

    @Enumerated(EnumType.STRING)
    @Column(name = "spraying_method", nullable = false)
    private SprayingMethod sprayingMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "spraying_frequency", nullable = false)
    private SprayingFrequency sprayingFrequency;

    @Column(name = "lighting_start_time")
    private LocalTime lightingStartTime;

    @Column(name = "lighting_end_time")
    private LocalTime lightingEndTime;

    @Column(name = "lighting_enabled", nullable = false)
    private Boolean lightingEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "heating_usage_mode", nullable = false)
    private HeatingUsageMode heatingUsageMode;

    @Column(name = "heating_start_time")
    private LocalTime heatingStartTime;

    @Column(name = "heating_end_time")
    private LocalTime heatingEndTime;

    public enum SprayingMethod {
        MANUAL, AUTOMATIC, BOTH
    }

    public enum SprayingFrequency {
        ONCE_A_DAY, TWICE_A_DAY, THREE_OR_MORE_A_DAY
    }

    public enum HeatingUsageMode {
        SCHEDULED, AUTO_BY_TEMPERATURE, WHEN_NEEDED, NOT_USED
    }

    public enum SprayingTime {
        MORNING, DAY, EVENING, NIGHT
    }

    @Builder
    public ManagementSetting(Enclosure enclosure, SprayingMethod sprayingMethod,
                             SprayingFrequency sprayingFrequency, LocalTime lightingStartTime,
                             LocalTime lightingEndTime, Boolean lightingEnabled,
                             HeatingUsageMode heatingUsageMode, LocalTime heatingStartTime,
                             LocalTime heatingEndTime) {
        this.enclosure = enclosure;
        this.sprayingMethod = sprayingMethod;
        this.sprayingFrequency = sprayingFrequency;
        this.lightingStartTime = lightingStartTime;
        this.lightingEndTime = lightingEndTime;
        this.lightingEnabled = lightingEnabled != null ? lightingEnabled : true;
        this.heatingUsageMode = heatingUsageMode;
        this.heatingStartTime = heatingStartTime;
        this.heatingEndTime = heatingEndTime;
    }
}