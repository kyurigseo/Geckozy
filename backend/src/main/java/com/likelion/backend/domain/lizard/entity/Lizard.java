package com.likelion.backend.domain.lizard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lizards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lizard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lizard_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "species_id", nullable = false)
    private Long speciesId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_date_unknown", nullable = false)
    private Boolean birthDateUnknown = false;

    @Column(name = "character_color", nullable = false, length = 20)
    private String characterColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "size_cm", precision = 6, scale = 2)
    private BigDecimal sizeCm;

    @Column(name = "weight_g", precision = 6, scale = 2)
    private BigDecimal weightG;

    @Column(name = "size_weight_unknown", nullable = false)
    private Boolean sizeWeightUnknown = false;

    public enum Gender {
        MALE, FEMALE
    }

    @Builder
    public Lizard(Long userId, Long speciesId, String name, LocalDate birthDate,
                  Boolean birthDateUnknown, String characterColor, Gender gender,
                  BigDecimal sizeCm, BigDecimal weightG, Boolean sizeWeightUnknown) {
        this.userId = userId;
        this.speciesId = speciesId;
        this.name = name;
        this.birthDate = birthDate;
        this.birthDateUnknown = birthDateUnknown != null ? birthDateUnknown : false;
        this.characterColor = characterColor;
        this.gender = gender;
        this.sizeCm = sizeCm;
        this.weightG = weightG;
        this.sizeWeightUnknown = sizeWeightUnknown != null ? sizeWeightUnknown : false;
    }
}