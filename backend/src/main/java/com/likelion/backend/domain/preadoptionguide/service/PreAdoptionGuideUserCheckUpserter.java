package com.likelion.backend.domain.preadoptionguide.service;

import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * 체크 상태 저장(UPSERT)을 연결된 DB에 맞는 쿼리로 실행한다.
 * - MySQL (운영 서버): INSERT ... ON DUPLICATE KEY UPDATE
 * - H2 (로컬 실행 / 테스트): MERGE INTO ... KEY
 *
 * 프로필 이름이 아니라 서버 시작 시 실제로 연결된 DB 종류를 보고 고르므로,
 * 로컬을 MySQL로 바꾸거나 H2의 MODE 설정이 달라도 그대로 동작한다.
 */
@Slf4j
@Component
public class PreAdoptionGuideUserCheckUpserter {

    enum Database {
        MYSQL, H2
    }

    private final PreAdoptionGuideUserCheckRepository userCheckRepository;
    private final Database database;

    public PreAdoptionGuideUserCheckUpserter(
            PreAdoptionGuideUserCheckRepository userCheckRepository,
            DataSource dataSource
    ) {
        this.userCheckRepository = userCheckRepository;
        this.database = detect(dataSource);
        log.info("[PreAdoptionGuide] 체크 상태 저장 쿼리: {}", database);
    }

    public void upsert(Long userId, Long checkItemId, boolean checked, LocalDateTime now) {
        switch (database) {
            case MYSQL -> userCheckRepository.upsertForMySql(userId, checkItemId, checked, now);
            case H2 -> userCheckRepository.upsertForH2(userId, checkItemId, checked, now);
        }
    }

    Database database() {
        return database;
    }

    static Database fromProductName(String productName) {
        if (productName != null) {
            String name = productName.toLowerCase();
            if (name.contains("mysql") || name.contains("mariadb")) {
                return Database.MYSQL;
            }
            if (name.contains("h2")) {
                return Database.H2;
            }
        }
        // 지원하지 않는 DB면 서버 시작 단계에서 바로 알 수 있게 실패시킨다
        throw new IllegalStateException("체크 상태 저장(UPSERT)을 지원하지 않는 DB입니다: " + productName);
    }

    private static Database detect(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return fromProductName(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalStateException("DB 종류를 확인할 수 없습니다.", e);
        }
    }
}
