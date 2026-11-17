package com.likelion.backend.domain.enclosure.service;

import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureCreateResponse;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateRequest;
import com.likelion.backend.domain.enclosure.dto.crud.EnclosureUpdateResponse;
import com.likelion.backend.domain.enclosure.dto.environment.*;
import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureGuidebookRecommendationResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureHumiditySuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.EnclosureTemperatureSuggestionResponse;
import com.likelion.backend.domain.enclosure.dto.suggestion.ManagementSettingRequest;
import com.likelion.backend.domain.enclosure.dto.suggestion.ManagementSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnclosureService {

    private final EnclosureCommandService enclosureCommandService;
    private final EnclosureRecommendationService enclosureRecommendationService;
    private final EnclosureEnvironmentService enclosureEnvironmentService;
    private final EnclosureCoachService enclosureCoachService;

    public EnclosureCreateResponse createEnclosure(EnclosureCreateRequest request, Long currentUserId) {
        return enclosureCommandService.createEnclosure(request, currentUserId);
    }

    public ManagementSettingResponse updateManagementSetting(Long enclosureId, ManagementSettingRequest request, Long currentUserId) {
        return enclosureCommandService.updateManagementSetting(enclosureId, request, currentUserId);
    }

    public EnclosureResponse getEnclosureByLizardId(Long lizardId, Long currentUserId) {
        return enclosureCommandService.getEnclosureByLizardId(lizardId, currentUserId);
    }

    @Transactional(readOnly = true)
    public EnclosureTemperatureSuggestionResponse getTemperatureSuggestions(Long enclosureId, Long currentUserId) {
        return enclosureRecommendationService.getTemperatureSuggestions(enclosureId, currentUserId);
    }

    @Transactional(readOnly = true)
    public EnclosureHumiditySuggestionResponse getHumiditySuggestions(Long enclosureId, Long currentUserId) {
        return enclosureRecommendationService.getHumiditySuggestions(enclosureId, currentUserId);
    }

    @Transactional(readOnly = true)
    public EnclosureGuidebookRecommendationResponse getGuidebookRecommendations(Long enclosureId, Long currentUserId) {
        return enclosureRecommendationService.getGuidebookRecommendations(enclosureId, currentUserId);
    }

    public EnclosureUpdateResponse updateEnclosure(Long enclosureId, EnclosureUpdateRequest requestDto, Long currentUserId) {
        return enclosureCommandService.updateEnclosure(enclosureId, requestDto, currentUserId);
    }

    public TemperatureStatusResponse getTemperatureStatus(Long enclosureId, Long currentUserId) {
        return enclosureEnvironmentService.getTemperatureStatus(enclosureId, currentUserId);
    }

    public HumidityStatusResponse getHumidityStatus(Long enclosureId, Long currentUserId) {
        return enclosureEnvironmentService.getHumidityStatus(enclosureId, currentUserId);
    }

    public AlertResponse getEnclosureAlerts(Long enclosureId, Long currentUserId) {
        return enclosureEnvironmentService.getEnclosureAlerts(enclosureId, currentUserId);
    }

    public TemperatureHistoryResponse getTemperatureHistory(Long enclosureId, String dateStr, Long currentUserId) {
        return enclosureEnvironmentService.getTemperatureHistory(enclosureId, dateStr, currentUserId);
    }

    public HumidityHistoryResponse getHumidityHistory(Long enclosureId, String dateStr, Long currentUserId) {
        return enclosureEnvironmentService.getHumidityHistory(enclosureId, dateStr, currentUserId);
    }

    public EnvironmentSummaryResponse getEnvironmentSummary(Long enclosureId, String dateStr, Long currentUserId) {
        return enclosureEnvironmentService.getEnvironmentSummary(enclosureId, dateStr, currentUserId);
    }

    public EnvironmentCoachResponse getEnvironmentCoach(Long enclosureId, Long currentUserId) {
        return enclosureCoachService.getCoach(enclosureId, currentUserId);
    }

    public TemperaturePatternResponse getTemperaturePattern(Long enclosureId, Long currentUserId) {
        return enclosureEnvironmentService.getTemperaturePattern(enclosureId, currentUserId);
    }
}
