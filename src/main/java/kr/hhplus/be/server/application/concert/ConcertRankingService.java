package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingDetail;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertRankingService {

    private final ConcertRankingRepository concertRankingRepository;
    private final ConcertRepository concertRepository;

    /**
     * 콘서트 매진 시점을 기준으로 랭킹 점수를 저장합니다.
     * 매진 시간(ms) = soldOutAt - openedAt
     * score가 작을수록 빠른 매진입니다.
     */
    public void recordSoldOutTime(Long concertId, long soldOutAt, long openedAt) {
        concertRankingRepository.saveSoldOutRanking(concertId, soldOutAt, openedAt);
    }
    /**
     * 상위 N개의 콘서트 랭킹을 상세 정보와 함께 조회합니다.
     */
    public List<ConcertRankingDetail> getTopConcertDetails(int limit) {
        List<ConcertRankingResult> rankings = concertRankingRepository.getTopRankings(limit);

        List<Long> ids = rankings.stream()
                .map(ConcertRankingResult::concertId)
                .toList();

        Map<Long, Concert> concertMap = ids.stream()
                .map(id -> concertRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "콘서트 ID=" + id + " 를 찾을 수 없습니다.")))
                .collect(Collectors.toMap(Concert::getId, Function.identity()));

        return rankings.stream()
                .map(result -> {
                    Concert concert = concertMap.get(result.concertId());
                    return new ConcertRankingDetail(
                            concert.getId(),
                            concert.getTitle(),
                            concert.getConcertDateTime(),
                            result.soldOutDurationMillis()
                    );
                })
                .toList();
    }
    /**
     * 오늘 랭킹 데이터를 초기화합니다.
     */
    public void clearTodayRanking() {
        concertRankingRepository.clearTodayRanking();
    }
}
