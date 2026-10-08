package com.likelion.backend.domain.enclosure.entity;

import com.likelion.backend.domain.concern.entity.Concern;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "enclosure_concerns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnclosureConcern {

    @EmbeddedId
    private EnclosureConcernId id;

    @MapsId("enclosureId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enclosure_id")
    private Enclosure enclosure;

    @MapsId("concernId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concern_id")
    private Concern concern;

    public EnclosureConcern(Enclosure enclosure, Concern concern) {
        this.enclosure = enclosure;
        this.concern = concern;
        this.id = new EnclosureConcernId(enclosure.getId(), concern.getId());
    }

    // --- 복합키 클래스 ---
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EnclosureConcernId implements Serializable {
        private Long enclosureId;
        private Long concernId;
    }
}