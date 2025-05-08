package kr.hhplus.be.server.support.scheduler;

import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.application.concert.ConcertCacheService;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 콘서트 캐시를 주기적으로 갱신하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertCacheScheduler {

    private static final String CACHE_NAME = "concertAll";
    private final ConcertCacheService concertCacheService;
    private final CacheManager cacheManager;

    /**
     * 매 1시간마다 캐시 갱신
     */
    @Scheduled(cron = "0 0 * * * *")  // 매 시간 정각마다
    public void refreshConcertCache() {
        log.info("[ConcertCacheScheduler] 콘서트 캐시 갱신 시작");

        List<ConcertResponse> latestConcerts = concertCacheService.getAllConcertResponses();
        Cache cache = cacheManager.getCache(CACHE_NAME);

        if (cache != null) {
            cache.put(CACHE_NAME, latestConcerts);
            log.info("[ConcertCacheScheduler] 콘서트 캐시 갱신 완료 - {}건", latestConcerts.size());
        } else {
            log.warn("[ConcertCacheScheduler] 캐시 객체를 찾을 수 없습니다: {}", CACHE_NAME);
            throw new CustomException(ErrorCode.CACHE_NOT_FOUND, "공연 목록 데이터를 저장할 캐시 공간을 찾을 수 없습니다.");
        }
    }
}
