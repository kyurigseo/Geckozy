package com.likelion.backend.domain.species.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "species")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "species_id")
    private Long speciesId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public Species(String name) {
        this.name = name;
    }
}
