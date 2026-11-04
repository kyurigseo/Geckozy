package com.likelion.backend.domain.carerecord.dto.response;

import com.likelion.backend.domain.carerecord.entity.CareRecordPhoto;
import lombok.Getter;

@Getter
public class CareRecordPhotoResponse {

    private final Long photoId;
    private final String imageUrl;

    public CareRecordPhotoResponse(CareRecordPhoto photo) {
        this.photoId = photo.getPhotoId();
        this.imageUrl = photo.getImageUrl();
    }
}