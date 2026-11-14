package com.likelion.backend.domain.enclosure.dto.environment;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EnclosureResponse {
    private Long enclosureId;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
    private String material;
    private String materialOther;
    private String ventilation;
    private String wallDesign;
    private String vineDesign;
    private String floorDesign;
    private String decorationDesign;
    private String concernDetail;

    public static EnclosureResponse from(Enclosure enclosure) {
        return EnclosureResponse.builder()
                .enclosureId(enclosure.getId())
                .width(enclosure.getWidth())
                .height(enclosure.getHeight())
                .depth(enclosure.getDepth())
                .material(enclosure.getMaterial() != null ? enclosure.getMaterial().name() : null)
                .materialOther(enclosure.getMaterialOther())
                .ventilation(enclosure.getVentilation() != null ? enclosure.getVentilation().name() : null)
                .wallDesign(enclosure.getWallDesign())
                .vineDesign(enclosure.getVineDesign())
                .floorDesign(enclosure.getFloorDesign())
                .decorationDesign(enclosure.getDecorationDesign())
                .concernDetail(enclosure.getConcernDetail())
                .build();
    }
}