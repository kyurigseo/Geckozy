package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest;
import com.likelion.backend.domain.preadoptionguide.dto.request.PreAdoptionGuideCheckSaveRequest.CheckDto;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideUserCheckRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideStepService;
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
 * 체크박스를 빠르게 연타해 같은 항목 저장 요청이 동시에 들어와도
 * 에러 없이 모두 성공하고 저장 행은 하나만 남는지 확인한다.
 * 실제 커밋이 필요하므로 @Transactional 을 붙이지 않고, 끝나면 만든 데이터를 지운다.
 */
@SpringBootTest
class PreAdoptionGuideCheckSaveConcurrencyTest {

    private static final int THREADS = 8;

    @Autowired
    private PreAdoptionGuideStepService stepService;

    @Autowired
    private PreAdoptionGuideService guideService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreAdoptionGuideUserCheckRepository userCheckRepository;

    private User user;

    @AfterEach
    void cleanUp() {
        if (user != null) {
            userCheckRepository.findAll().stream()
                    .filter(check -> check.getUser().getUserId().equals(user.getUserId()))
                    .forEach(userCheckRepository::delete);
            userRepository.delete(user);
        }
    }

    @Test
    @DisplayName("같은 항목 저장 요청이 동시에 들어와도 모두 성공하고 저장 행은 하나")
    void saveChecks_ConcurrentSameItem_SingleRow() throws Exception {
        user = userRepository.save(new User("check-concurrency", "check-concurrency@example.com", "password"));
        Long speciesId = speciesRepository.findByName("크레스티드 게코").orElseThrow().getSpeciesId();
        Long preGuideId = guideService.getGuideBySpecies(speciesId).getPreGuideId();
        Long itemId = stepService.getStep(user.getUserId(), preGuideId, 2).getCheckItems().get(0).getCheckItemId();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREADS; i++) {
                boolean checked = i % 2 == 0;
                futures.add(executor.submit(() -> {
                    start.await();
                    return stepService.saveChecks(user.getUserId(), preGuideId, 2,
                            new PreAdoptionGuideCheckSaveRequest(List.of(new CheckDto(itemId, checked)))
                    ).getSavedCount();
                }));
            }
            start.countDown();

            for (Future<Integer> future : futures) {
                assertThat(future.get(10, TimeUnit.SECONDS)).isEqualTo(1);
            }

            assertThat(userCheckRepository
                    .findAllByUser_UserIdAndCheckItem_CheckItemIdIn(user.getUserId(), List.of(itemId)))
                    .hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
