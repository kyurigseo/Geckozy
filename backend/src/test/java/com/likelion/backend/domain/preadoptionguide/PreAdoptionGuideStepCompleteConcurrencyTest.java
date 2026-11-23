package com.likelion.backend.domain.preadoptionguide;

import com.likelion.backend.domain.enclosure.entity.Enclosure;
import com.likelion.backend.domain.enclosure.repository.EnclosureRepository;
import com.likelion.backend.domain.lizard.entity.Lizard;
import com.likelion.backend.domain.lizard.repository.LizardRepository;
import com.likelion.backend.domain.preadoptionguide.dto.response.PreAdoptionGuideStepCompleteResponse;
import com.likelion.backend.domain.preadoptionguide.repository.PreAdoptionGuideProgressRepository;
import com.likelion.backend.domain.preadoptionguide.service.PreAdoptionGuideService;
import com.likelion.backend.domain.species.entity.Species;
import com.likelion.backend.domain.species.repository.SpeciesRepository;
import com.likelion.backend.domain.user.entity.User;
import com.likelion.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 진행 상태 조회 없이 완료 버튼을 연타해 같은 단계 완료 요청이 동시에 들어와도
 * 모두 성공하고 진행 상태는 하나만 생성되는지 확인한다.
 * 실제 커밋이 필요하므로 @Transactional 을 붙이지 않고, 끝나면 만든 데이터를 지운다.
 */
@SpringBootTest
class PreAdoptionGuideStepCompleteConcurrencyTest {

    private static final int THREADS = 8;

    @Autowired
    private PreAdoptionGuideService guideService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LizardRepository lizardRepository;

    @Autowired
    private EnclosureRepository enclosureRepository;

    @Autowired
    private PreAdoptionGuideProgressRepository progressRepository;

    private User user;
    private Lizard lizard;
    private Enclosure enclosure;

    @AfterEach
    void cleanUp() {
        if (user == null) {
            return;
        }
        progressRepository.findAll().stream()
                .filter(progress -> progress.getUser().getUserId().equals(user.getUserId()))
                .forEach(progressRepository::delete);
        if (enclosure != null) {
            enclosureRepository.delete(enclosure);
        }
        if (lizard != null) {
            lizardRepository.delete(lizard);
        }
        userRepository.delete(user);
    }

    @Test
    @DisplayName("같은 단계 완료 요청이 동시에 들어와도 모두 성공하고 진행 상태는 하나")
    void completeStep_ConcurrentSameStep() throws Exception {
        user = userRepository.save(new User("complete-concurrency", "complete-concurrency@example.com", "password"));
        Species crestedGecko = speciesRepository.findByName("크레스티드 게코").orElseThrow();
        lizard = lizardRepository.save(Lizard.builder()
                .user(user)
                .species(crestedGecko)
                .name("게코")
                .birthDateUnknown(true)
                .characterColor("yellow")
                .gender(Lizard.Gender.FEMALE)
                .sizeWeightUnknown(true)
                .build());
        enclosure = enclosureRepository.save(Enclosure.builder()
                .lizard(lizard)
                .width(new BigDecimal("45"))
                .depth(new BigDecimal("45"))
                .height(new BigDecimal("60"))
                .material(Enclosure.Material.GLASS)
                .ventilation(Enclosure.Ventilation.TOP)
                .build());
        Long preGuideId = guideService.getGuideBySpecies(crestedGecko.getSpeciesId()).getPreGuideId();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<PreAdoptionGuideStepCompleteResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREADS; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return guideService.completeStep(user.getUserId(), preGuideId, 1);
                }));
            }
            start.countDown();

            for (Future<PreAdoptionGuideStepCompleteResponse> future : futures) {
                PreAdoptionGuideStepCompleteResponse response = future.get(10, TimeUnit.SECONDS);
                assertThat(response.getCompletedStep()).isEqualTo(1);
                assertThat(response.getCurrentStep()).isEqualTo(2);
            }

            assertThat(progressRepository.findAll().stream()
                    .filter(progress -> progress.getUser().getUserId().equals(user.getUserId())))
                    .hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
