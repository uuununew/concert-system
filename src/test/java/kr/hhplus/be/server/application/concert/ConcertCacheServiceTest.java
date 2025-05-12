package kr.hhplus.be.server.application.concert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.cache.CacheConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcertCacheServiceTest {

    private static final String CACHE_NAME = CacheConstants.CONCERT_ALL_CACHE;

    @Autowired
    private ConcertCacheService concertCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
        Pageable pageable = PageRequest.of(0, 10);

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
        Pageable pageable = PageRequest.of(0, 10);

        // 1차 호출 - 캐시 저장
        concertCacheService.getAllConcertResponses();

        reset(concertRepository);

        // 실제 캐시에서 값 꺼내 확인
        String key = "page:0:10";
        Cache.ValueWrapper wrapper = cacheManager.getCache(CacheConstants.CONCERT_ALL_CACHE).get("all");
        assertThat(wrapper).isNotNull();

        @SuppressWarnings("unchecked")
        List<LinkedHashMap<String, Object>> raw = (List<LinkedHashMap<String, Object>>) wrapper.get();

        List<ConcertResponse> cached = raw.stream()
                .map(map -> objectMapper.convertValue(map, ConcertResponse.class))
                .toList();

        // then
        assertThat(cached).hasSize(1);
        assertThat(cached.get(0).getTitle()).isEqualTo("IU");

        // 다시 호출 시 hit 확인
        List<ConcertResponse> secondCall = concertCacheService.getAllConcertResponses();
        assertThat(secondCall).hasSize(1);
        verify(concertRepository, never()).findAll();
    }
}