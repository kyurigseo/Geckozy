package com.likelion.backend.domain.enclosure.controller;

import com.likelion.backend.domain.enclosure.dto.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.service.EnclosureService;
import com.likelion.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enclosures")
@RequiredArgsConstructor
public class EnclosureController {

    private final EnclosureService enclosureService;

    @GetMapping("/{enclosureId}/temperatures/suggestions")
    public ResponseEntity<ApiResponse<EnclosureTemperatureSuggestionResponse>> getTemperatureSuggestions(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        EnclosureTemperatureSuggestionResponse response = enclosureService.getTemperatureSuggestions(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{enclosureId}/humidities/suggestions")
    public ResponseEntity<ApiResponse<EnclosureHumiditySuggestionResponse>> getHumiditySuggestions(
            @PathVariable("enclosureId") Long enclosureId
    ) {
        EnclosureHumiditySuggestionResponse response = enclosureService.getHumiditySuggestions(enclosureId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
