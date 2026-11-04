package com.likelion.backend.domain.enclosure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enclosure_components")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnclosureComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enclosure_id", nullable = false)
    private Enclosure enclosure;

    @Column(name = "component_category", nullable = false, length = 30)
    private String componentCategory;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Builder
    public EnclosureComponent(Enclosure enclosure, String componentCategory, String componentName) {
        this.enclosure = enclosure;
        this.componentCategory = componentCategory;
        this.componentName = componentName;
    }
}