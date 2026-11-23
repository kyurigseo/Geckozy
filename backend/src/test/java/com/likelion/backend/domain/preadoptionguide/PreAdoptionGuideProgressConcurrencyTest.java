package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideProgressResponse;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 진행 상태 첫 조회가 동시에 들어와도 에러 없이 같은 진행 기록 하나만 만들어지는지 확인한다.
 * 실제 커밋이 필요하므로 @Transactional 을 붙이지 않고, 끝나면 만든 데이터를 지운다.
 */
@SpringBootTest
class PreAdoptionGuideProgressConcurrencyTest {

    private static final int THREADS = 8;

    @Autowired
    private PreAdoptionGuideService guideService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreAdoptionGuideProgressRepository progressRepository;

    private User user;

    @AfterEach
    void cleanUp() {
        if (user != null) {
            progressRepository.findAll().stream()
                    .filter(progress -> progress.getUser().getUserId().equals(user.getUserId()))
                    .forEach(progressRepository::delete);
            userRepository.delete(user);
        }
    }

    @Test
    @DisplayName("같은 사용자의 첫 진행 상태 조회가 동시에 들어와도 모두 성공하고 기록은 하나만 생성")
    void getProgress_ConcurrentFirstRequests_CreateSingleProgress() throws Exception {
        user = userRepository.save(new User("concurrency-tester", "concurrency-tester@example.com", "password"));
        Long speciesId = speciesRepository.findByName("크레스티드 게코").orElseThrow().getSpeciesId();
        Long preGuideId = guideService.getGuideBySpecies(speciesId).getPreGuideId();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<PreAdoptionGuideProgressResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREADS; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return guideService.getProgress(user.getUserId(), preGuideId);
                }));
            }
            start.countDown();

            List<Long> progressIds = new ArrayList<>();
            for (Future<PreAdoptionGuideProgressResponse> future : futures) {
                progressIds.add(future.get(10, TimeUnit.SECONDS).getProgressId());
            }

            assertThat(progressIds).hasSize(THREADS);
            assertThat(progressIds.stream().distinct()).hasSize(1);
            assertThat(progressRepository
                    .findByUser_UserIdAndPreAdoptionGuide_PreGuideId(user.getUserId(), preGuideId))
                    .isPresent();
        } finally {
            executor.shutdownNow();
        }
    }
}
