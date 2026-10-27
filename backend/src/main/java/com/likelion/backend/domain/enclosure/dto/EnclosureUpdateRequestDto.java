package com.likelion.backend.domain.enclosure.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class EnclosureUpdateRequestDto {

    @NotNull(message = "사육장 가로 크기는 필수입니다.")
    private BigDecimal width;

    @NotNull(message = "사육장 높이 크기는 필수입니다.")
    private BigDecimal height;

    @NotNull(message = "사육장 세로 크기는 필수입니다.")
    private BigDecimal depth;

    @NotNull(message = "사육장 재질은 필수입니다.")
    private String material;

    private String materialOther;

    @NotNull(message = "환기 구조는 필수입니다.")
    private String ventilation;

    private String wallDesign;
    private String vineDesign;
    private String floorDesign;
    private String decorationDesign;

    private List<ComponentDto> components;
    private List<Long> concernIds;
    private String concernDetail;

    @Getter
    @NoArgsConstructor
    public static class ComponentDto {
        private String componentCategory;
        private String componentName;
    }
}