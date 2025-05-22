package kr.hhplus.be.server.infrastructure.concert.ranking;

import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import kr.hhplus.be.server.domain.concert.ranking.DailyConcertRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConcertRankingRedisRepository implements ConcertRankingRepository {

    private static final String RANKING_KEY_PREFIX = "concert-soldout-ranking:";
    private static final long TTL_SECONDS = 60 * 60 * 24; // 1일

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveSoldOutRanking(Long concertId, long soldOutAtMillis, long openedAtMillis) {
        long duration = soldOutAtMillis - openedAtMillis; // 매진까지 걸린 시간
        String key = getTodayKey();

        redisTemplate.opsForZSet().add(key, String.valueOf(concertId), duration);
        redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
        log.info("매진 랭킹 저장: concertId={}, duration={}ms", concertId, duration);
    }

    @Override
    public List<ConcertRankingResult> getTopRankings(int limit) {
        String key = getTodayKey();
        Set<ZSetOperations.TypedTuple<String>> range =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, limit - 1);

        if (range == null || range.isEmpty()) {
            return Collections.emptyList();
        }

        return range.stream()
                .map(tuple -> new ConcertRankingResult(
                        Long.valueOf(tuple.getValue()),
                        tuple.getScore()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void clearTodayRanking() {
        String key = getTodayKey();
        Boolean deleted = redisTemplate.delete(key);
        log.info("오늘 랭킹 초기화: key={}, deleted={}", key, deleted);
    }

        private String getTodayKey() {
            return RANKING_KEY_PREFIX + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }
}
