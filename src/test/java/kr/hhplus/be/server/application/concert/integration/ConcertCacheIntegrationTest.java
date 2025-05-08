package kr.hhplus.be.server.application.concert.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.application.concert.ConcertCacheService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.scheduler.ConcertCacheScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcertCacheIntegrationTest {

    private static final String CACHE_NAME = "concertAll";

    @Autowired
    private ConcertCacheService concertCacheService;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    ConcertCacheScheduler concertCacheScheduler;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CACHE_NAME).clear();
        concertRepository.deleteAll();
    }

    @Test
    @DisplayName("캐시 miss 시 DB 조회 + 캐시 저장, 두 번째 호출 시 캐시 hit")
    void cache_miss_then_hit() {
        // given
        concertRepository.save(new Concert("BTS", 1, ConcertStatus.OPENED, LocalDateTime.now()));

        // when
        List<ConcertResponse> firstCall = concertCacheService.getAllConcertResponses();
        List<ConcertResponse> secondCall = concertCacheService.getAllConcertResponses();

        // then
        assertThat(firstCall).hasSize(1);
        assertThat(firstCall.get(0).getTitle()).isEqualTo("BTS");

        assertThat(secondCall).hasSize(1);
        assertThat(secondCall.get(0).getTitle()).isEqualTo("BTS");

        // 캐시에 값이 저장되었는지 직접 확인
        Cache cache = cacheManager.getCache(CACHE_NAME);

        @SuppressWarnings("unchecked")
        List<ConcertResponse> cached = (List<ConcertResponse>) cache.get(SimpleKey.EMPTY, List.class);

        assertThat(cached).isNotNull();
        assertThat(cached).hasSize(1);
    }

    @Test
    @DisplayName("콘서트 등록 시 캐시 무효화")
    void register_concert_should_evict_cache() {
        // given
        concertRepository.save(new Concert("BTS", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));
        concertCacheService.getAllConcertResponses();

        assertThat(cacheManager.getCache(CACHE_NAME).get(SimpleKey.EMPTY)).isNotNull();

        // when
        concertService.registerConcert(new CreateConcertCommand("IU", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));

        // then
        assertThat(cacheManager.getCache(CACHE_NAME).get(SimpleKey.EMPTY)).isNull();
    }

    @Test
    @DisplayName("스케줄러 캐시 갱신 시 캐시 데이터 업데이트")
    void scheduler_refresh_cache() {
        // given
        concertRepository.save(new Concert("BTS", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));
        concertCacheService.getAllConcertResponses(); // 캐시 저장

        // when
        concertService.registerConcert(new CreateConcertCommand(
                "IU", 2, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));
        concertCacheScheduler.refreshConcertCache();

        // then
        List<ConcertResponse> cachedResponses = concertCacheService.getAllConcertResponses();

        assertThat(cachedResponses)
                .isNotNull()
                .hasSize(2)
                .extracting("title")
                .contains("IU", "BTS");
    }
}