package com.likelion.backend.domain.preadoptionguide.entity;

import com.likelion.backend.domain.species.entity.Species;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pre_adoption_guides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreAdoptionGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pre_guide_id")
    private Long preGuideId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "species_id", nullable = false, unique = true)
    private Species species;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public PreAdoptionGuide(Species species, String title, String description) {
        this.species = species;
        this.title = title;
        this.description = description;
    }
}
