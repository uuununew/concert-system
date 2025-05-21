package kr.hhplus.be.server.support.scheduler;

import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import kr.hhplus.be.server.domain.concert.ranking.DailyConcertRanking;
import kr.hhplus.be.server.infrastructure.concert.ranking.DailyConcertRankingJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertDailyRankingScheduler {

    private final ConcertRankingRepository concertRankingRepository;
    private final DailyConcertRankingJpaRepository dailyRankingJpaRepository;

    /**
     * 매일 자정에 Redis에 저장된 콘서트 랭킹을 DB로 저장합니다.
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00
    @Transactional
    public void saveTodayRankings() {
        log.info("일간 콘서트 랭킹 저장 시작");

        List<ConcertRankingResult> topRankings = concertRankingRepository.getTopRankings(100);
        LocalDate today = LocalDate.now();

        List<DailyConcertRanking> entities = topRankings.stream()
                .map(result -> new DailyConcertRanking(
                        null,
                        result.concertId(),
                        result.soldOutDurationMillis(),
                        today
                ))
                .toList();

        dailyRankingJpaRepository.saveAll(entities);
        log.info("일간 콘서트 랭킹 저장 완료: {}건", entities.size());
    }
}