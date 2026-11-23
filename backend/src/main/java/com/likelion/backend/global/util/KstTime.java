package com.likelion.backend.global.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** 서버 기준 시간대(KST) 변환 유틸 */
public final class KstTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private KstTime() {}

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDateTime toLocal(OffsetDateTime time) {
        return time.atZoneSameInstant(ZONE).toLocalDateTime();
    }

    public static OffsetDateTime toOffset(LocalDateTime time) {
        return time == null ? null : time.atZone(ZONE).toOffsetDateTime();
    }
}
