package kr.hhplus.be.server.domain.concert.ranking;

import java.util.List;

public interface ConcertRankingRepository {

    /**
     * 콘서트가 매진되었을 때, 매진까지 걸린 시간을 랭킹에 기록합니다.
     * 점수(score)는 매진까지 걸린 시간(ms)이며, 낮을수록 빠른 매진입니다.
     *
     * @param concertId 콘서트 ID
     * @param soldOutAtMillis 매진 시각 (밀리초)
     * @param openedAtMillis 오픈 시각 (밀리초)
     */
    void saveSoldOutRanking(Long concertId, long soldOutAtMillis, long openedAtMillis);

    /**
     * 빠른 매진 순으로 상위 콘서트 목록을 조회합니다.
     *
     * @param limit 최대 조회 수
     * @return 빠른 매진 랭킹 결과 목록
     */
    List<ConcertRankingResult> getTopRankings(int limit);

    /**
     * 오늘 날짜 기준 랭킹 데이터를 초기화합니다.
     */
    void clearTodayRanking();
}
