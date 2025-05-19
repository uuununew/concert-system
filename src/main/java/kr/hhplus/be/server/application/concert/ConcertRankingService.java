package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertRankingService {

    private final ConcertRankingRepository concertRankingRepository;

    /**
     * 콘서트 매진 시점을 기준으로 랭킹 점수를 저장합니다.
     * 매진 시간(ms) = soldOutAt - openedAt
     * score가 작을수록 빠른 매진입니다.
     */
    public void recordSoldOutTime(Long concertId, long soldOutAt, long openedAt) {
        concertRankingRepository.saveSoldOutRanking(concertId, soldOutAt, openedAt);
    }
    /**
     * 상위 N개의 콘서트 랭킹을 조회합니다.
     * score가 낮을수록 빠른 매진입니다.
     */
    public List<ConcertRankingResult> getTopConcerts(int limit) {
        return concertRankingRepository.getTopRankings(limit);
    }
    /**
     * 오늘 랭킹 데이터를 초기화합니다.
     */
    public void clearTodayRanking() {
        concertRankingRepository.clearTodayRanking();
    }
}
