package com.likelion.backend.domain.species.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "species_environment_standards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpeciesEnvironmentStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_id")
    private Long standardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false, unique = true)
    private Species species;

    @Column(name = "day_temperature_min", precision = 5, scale = 2)
    private BigDecimal dayTemperatureMin;

    @Column(name = "day_temperature_max", precision = 5, scale = 2)
    private BigDecimal dayTemperatureMax;

    @Column(name = "night_temperature_min", precision = 5, scale = 2)
    private BigDecimal nightTemperatureMin;

    @Column(name = "night_temperature_max", precision = 5, scale = 2)
    private BigDecimal nightTemperatureMax;

    @Column(name = "temperature_caution", columnDefinition = "TEXT")
    private String temperatureCaution;

    @Column(name = "humidity_min", precision = 5, scale = 2)
    private BigDecimal humidityMin;

    @Column(name = "humidity_max", precision = 5, scale = 2)
    private BigDecimal humidityMax;

    @Column(name = "humidity_caution", columnDefinition = "TEXT")
    private String humidityCaution;

    @Builder
    public SpeciesEnvironmentStandard(
            Species species,
            BigDecimal dayTemperatureMin,
            BigDecimal dayTemperatureMax,
            BigDecimal nightTemperatureMin,
            BigDecimal nightTemperatureMax,
            String temperatureCaution,
            BigDecimal humidityMin,
            BigDecimal humidityMax,
            String humidityCaution
    ) {
        this.species = species;
        this.dayTemperatureMin = dayTemperatureMin;
        this.dayTemperatureMax = dayTemperatureMax;
        this.nightTemperatureMin = nightTemperatureMin;
        this.nightTemperatureMax = nightTemperatureMax;
        this.temperatureCaution = temperatureCaution;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
        this.humidityCaution = humidityCaution;
    }
}