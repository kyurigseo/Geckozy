package com.likelion.backend.domain.lizard.entity;

import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    private Long lizardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_date_unknown", nullable = false)
    private boolean birthDateUnknown = false;

    @Column(name = "character_color", nullable = false, length = 20)
    private String characterColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "size_cm", precision = 6, scale = 2)
    private BigDecimal sizeCm;

    @Column(name = "weight_g", precision = 6, scale = 2)
    private BigDecimal weightG;

    @Column(name = "size_weight_unknown", nullable = false)
    private boolean sizeWeightUnknown = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum Gender {
        MALE, FEMALE
    }

    @Builder
    public Lizard(
            User user,
            Species species,
            String name,
            LocalDate birthDate,
            boolean birthDateUnknown,
            String characterColor,
            Gender gender,
            BigDecimal sizeCm,
            BigDecimal weightG,
            boolean sizeWeightUnknown
    ) {
        this.user = user;
        this.species = species;
        this.name = name;
        this.birthDate = birthDate;
        this.birthDateUnknown = birthDateUnknown;
        this.characterColor = characterColor;
        this.gender = gender;
        this.sizeCm = sizeCm;
        this.weightG = weightG;
        this.sizeWeightUnknown = sizeWeightUnknown;
    }

    public void update(
            Species species,
            String name,
            LocalDate birthDate,
            Boolean birthDateUnknown,
            String characterColor,
            Gender gender,
            BigDecimal sizeCm,
            BigDecimal weightG
    ) {
        if (species != null) {
            this.species = species;
        }

        if (name != null) {
            this.name = name;
        }

        if (birthDate != null) {
            this.birthDate = birthDate;
        }

        if (birthDateUnknown != null) {
            this.birthDateUnknown = birthDateUnknown;
        }

        if (characterColor != null) {
            this.characterColor = characterColor;
        }

        if (gender != null) {
            this.gender = gender;
        }

        if (sizeCm != null) {
            this.sizeCm = sizeCm;
        }

        if (weightG != null) {
            this.weightG = weightG;
        }

        // 크기나 몸무게 값을 입력하면 "모름" 상태 해제
        if (sizeCm != null || weightG != null) {
            this.sizeWeightUnknown = false;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}