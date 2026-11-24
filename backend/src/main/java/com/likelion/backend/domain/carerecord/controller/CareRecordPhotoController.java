package com.likelion.backend.domain.carerecord.controller;

import com.likelion.backend.domain.carerecord.service.CareRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads/care-records")
public class CareRecordPhotoController {

    private final CareRecordService careRecordService;

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getPhoto(
            @AuthenticationPrincipal Long userId,
            @PathVariable("filename") String filename
    ) {
        Resource resource =
                careRecordService.getPhotoFile(userId, filename);

        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}