package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideUserCheckUpserter.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PreAdoptionGuideUserCheckUpserterTest {

    @Autowired
    private PreAdoptionGuideUserCheckUpserter upserter;

    @Test
    @DisplayName("1. DB 이름으로 쿼리 종류를 고름")
    void fromProductName() {
        assertThat(PreAdoptionGuideUserCheckUpserter.fromProductName("MySQL")).isEqualTo(Database.MYSQL);
        assertThat(PreAdoptionGuideUserCheckUpserter.fromProductName("MariaDB")).isEqualTo(Database.MYSQL);
        assertThat(PreAdoptionGuideUserCheckUpserter.fromProductName("H2")).isEqualTo(Database.H2);
    }

    @Test
    @DisplayName("2. 지원하지 않는 DB면 서버 시작 단계에서 실패")
    void unsupportedDatabase() {
        assertThatThrownBy(() -> PreAdoptionGuideUserCheckUpserter.fromProductName("PostgreSQL"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> PreAdoptionGuideUserCheckUpserter.fromProductName(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("3. 테스트 환경(H2)에서는 H2 쿼리를 사용")
    void usesH2InTests() {
        assertThat(upserter.database()).isEqualTo(Database.H2);
    }
}
