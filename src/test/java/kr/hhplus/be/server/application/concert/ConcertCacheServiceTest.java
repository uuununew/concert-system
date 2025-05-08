package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcertCacheServiceTest {

    private static final String CACHE_NAME = "concertAll";

    @Autowired
    private ConcertCacheService concertCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ConcertRepository concertRepository;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CACHE_NAME).clear();
        reset(concertRepository);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CacheManager cacheManager() {
            SimpleCacheManager manager = new SimpleCacheManager();
            ConcurrentMapCache cache = new ConcurrentMapCache(CACHE_NAME, false);
            manager.setCaches(List.of(cache));
            // 초기화 명시적으로 추가
            manager.afterPropertiesSet();
            return manager;
        }

        @Bean
        @Primary
        public ConcertRepository concertRepository() {
            return mock(ConcertRepository.class);
        }
    }

    @Test
    @DisplayName("캐시 miss 시 repository 호출하여 데이터 반환")
    void getAllConcertResponses_cacheMiss() {
        // given
        List<Concert> concerts = List.of(new Concert("BTS", 1, null, LocalDateTime.now()));
        when(concertRepository.findAll()).thenReturn(concerts);

        // when
        List<ConcertResponse> responses = concertCacheService.getAllConcertResponses();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("BTS");
        verify(concertRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("캐시 hit 시 repository 호출 없이 캐시 데이터 반환")
    void getAllConcertResponses_cacheHit() {
        // given
        List<Concert> concerts = List.of(new Concert("IU", 1, null, LocalDateTime.now()));
        when(concertRepository.findAll()).thenReturn(concerts);

        concertCacheService.getAllConcertResponses();

        reset(concertRepository);

        // when: 두 번째 호출 - 캐시에서 가져와야 함
        List<ConcertResponse> responses = concertCacheService.getAllConcertResponses();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("IU");
        verify(concertRepository, never()).findAll();
    }
}