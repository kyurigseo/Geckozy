package com.likelion.backend.domain.carerecord.controller;

import com.likelion.backend.domain.carerecord.dto.request.CareRecordCreateRequest;
import com.likelion.backend.domain.carerecord.dto.request.CareRecordMemoUpdateRequest;
import com.likelion.backend.domain.carerecord.dto.response.*;
import com.likelion.backend.domain.carerecord.service.CareRecordService;
import com.likelion.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lizards/{lizardId}/care-records")
public class CareRecordController {

    private final CareRecordService careRecordService;

    // REC-01. 월별 기록 캘린더 조회
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<CareRecordCalendarResponse>> getCalendar(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getCalendar(userId, lizardId, year, month)
                )
        );
    }

    // REC-02. 날짜별 기록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CareRecordListResponse>>> getRecordsByDate(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getRecordsByDate(userId, lizardId, date)
                )
        );
    }

    // REC-03~11. 기록 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CareRecordCreateResponse>> createRecord(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @RequestBody CareRecordCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        careRecordService.createRecord(userId, lizardId, request)
                )
        );
    }

    // REC-12. 사진 첨부
    @PostMapping(
            value = "/{recordId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CareRecordPhotoResponse>> addPhoto(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        careRecordService.addPhoto(userId, recordId, file)
                )
        );
    }

    // REC-13. 메모 수정
    @PatchMapping("/{recordId}/memo")
    public ResponseEntity<ApiResponse<Void>> updateMemo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId,
            @RequestBody CareRecordMemoUpdateRequest request
    ) {
        careRecordService.updateMemo(userId, recordId, request.getMemo());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // REC-14. 기록 시점 환경 정보 조회
    @GetMapping("/{recordId}/environment")
    public ResponseEntity<ApiResponse<EnvironmentResponse>> getEnvironment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getEnvironment(userId, recordId)
                )
        );
    }

    // REC-15. 기록 상세 조회
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<CareRecordDetailResponse>> getRecordDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getRecordDetail(userId, recordId)
                )
        );
    }

    // REC-16. 환경 그래프 조회
    @GetMapping("/{recordId}/environment/graph")
    public ResponseEntity<ApiResponse<List<EnvironmentGraphResponse>>> getEnvironmentGraph(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId,
            @RequestParam(defaultValue = "30") int beforeMinutes,
            @RequestParam(defaultValue = "30") int afterMinutes
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getEnvironmentGraph(
                                userId, recordId, beforeMinutes, afterMinutes
                        )
                )
        );
    }

    // REC-17. 가장 가까운 환경 정보 조회
    @GetMapping("/{recordId}/environment/nearest")
    public ResponseEntity<ApiResponse<EnvironmentResponse>> getNearestEnvironment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getNearestEnvironment(userId, recordId)
                )
        );
    }

    // REC-18. 환경 정보 요약
    @GetMapping("/{recordId}/environment/summary")
    public ResponseEntity<ApiResponse<EnvironmentSummaryResponse>> getEnvironmentSummary(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @PathVariable Long recordId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getEnvironmentSummary(userId, recordId)
                )
        );
    }

    // REC-19. 월별 기록 변화 조회
    @GetMapping("/monthly-changes")
    public ResponseEntity<ApiResponse<MonthlyChangesResponse>> getMonthlyChanges(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lizardId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        careRecordService.getMonthlyChanges(userId, lizardId, year, month)
                )
        );
    }
}