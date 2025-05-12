package kr.hhplus.be.server.application.concert.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.concert.ConcertCacheService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.cache.CacheConstants;
import kr.hhplus.be.server.support.config.RedisCacheTestConfig;
import kr.hhplus.be.server.support.scheduler.ConcertCacheScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisCacheTestConfig.class)
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

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).clear();
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
        assertThat(secondCall).hasSize(1);

        // Redis에서 직접 캐시 값 조회 → LinkedHashMap을 ConcertResponse로 변환
        Cache.ValueWrapper wrapper = cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).get("all");
        assertThat(wrapper).isNotNull();

        @SuppressWarnings("unchecked")
        List<LinkedHashMap<String, Object>> raw = (List<LinkedHashMap<String, Object>>) wrapper.get();
        List<ConcertResponse> cached = raw.stream()
                .map(map -> objectMapper.convertValue(map, ConcertResponse.class))
                .toList();

        assertThat(cached).hasSize(1);
        assertThat(cached.get(0).getTitle()).isEqualTo("BTS");
    }

    @Test
    @DisplayName("콘서트 등록 시 캐시 무효화")
    void register_concert_should_evict_cache() {
        // given
        concertRepository.save(new Concert("BTS", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));
        concertCacheService.getAllConcertResponses();

        assertThat(cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).get("all")).isNotNull();

        // when
        concertService.registerConcert(new CreateConcertCommand("IU", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)));

        // then
        assertThat(cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).get(SimpleKey.EMPTY)).isNull();
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

    @Test
    @DisplayName("TTL 만료 후 캐시가 자동 제거되고 다시 조회 시 DB 접근")
    void cache_should_expire_after_ttl() throws InterruptedException {
        concertRepository.save(new Concert("BTS", 1, ConcertStatus.OPENED, LocalDateTime.now()));
        concertCacheService.getAllConcertResponses();

        Thread.sleep(3500); // TTL 3초 이상 기다림

        assertThat(cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).get(SimpleKey.EMPTY)).isNull(); // TTL 만료 확인
    }
}