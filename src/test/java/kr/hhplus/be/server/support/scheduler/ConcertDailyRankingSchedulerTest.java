package kr.hhplus.be.server.support.scheduler;

import kr.hhplus.be.server.domain.concert.ranking.DailyConcertRanking;
import kr.hhplus.be.server.infrastructure.concert.ranking.DailyConcertRankingJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ConcertDailyRankingSchedulerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ConcertDailyRankingScheduler scheduler;

    @Autowired
    private DailyConcertRankingJpaRepository rankingRepository;

    private static final String RANKING_KEY = "concert-soldout-ranking:" + LocalDate.now();

    @BeforeEach
    void setUp() {
        // Redis에 미리 랭킹 데이터 삽입
        redisTemplate.opsForZSet().add(RANKING_KEY, "1", 5000);  // concertId=1, duration=5s
        redisTemplate.opsForZSet().add(RANKING_KEY, "2", 8000);  // concertId=2, duration=8s
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete(RANKING_KEY); // Redis 정리
        rankingRepository.deleteAll();     // DB 정리
    }

    @Test
    @DisplayName("일간 랭킹 저장: Redis -> DB")
    void saveTodayRankings_success() {
        // when
        scheduler.saveTodayRankings();

        // then
        List<DailyConcertRanking> rankings = rankingRepository.findAll();
        assertThat(rankings).hasSize(2);

        assertThat(rankings)
                .extracting(DailyConcertRanking::getConcertId)
                .containsExactlyInAnyOrder(1L, 2L);

        assertThat(rankings)
                .extracting(DailyConcertRanking::getSoldOutDurationMillis)
                .containsExactlyInAnyOrder(5000.0, 8000.0);
    }
}