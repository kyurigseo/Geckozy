package com.likelion.backend.domain.concern.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concerns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concern_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public Concern(String name) {
        this.name = name;
    }
}