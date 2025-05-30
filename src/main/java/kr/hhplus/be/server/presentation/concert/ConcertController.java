package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.concert.ConcertCacheService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.ConcertRankingService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingDetail;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import kr.hhplus.be.server.domain.concert.ranking.DailyConcertRanking;
import kr.hhplus.be.server.infrastructure.concert.ranking.DailyConcertRankingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertCacheService concertCacheService;
    private final ConcertRankingService concertRankingService;
    private final DailyConcertRankingJpaRepository dailyConcertRankingJpaRepository;

    /**
     * [POST] /concerts
     * 콘서트 등록 API
     */
    @PostMapping
    public ConcertResponse registerConcert(@RequestBody @Valid CreateConcertRequest request) {
        Concert concert = concertService.registerConcert(request.toCommand());
        return ConcertResponse.from(concert);
    }

    /**
     * 공연 상태 API
     * PATCH /concerts/{concertId}/status
     */
    @PatchMapping("/{concertId}/status")
    public void changeConcertStatus(
            @PathVariable Long concertId,
            @RequestBody @Valid ChangeConcertStatusRequest request
    ) {
        concertService.changeConcertStatus(concertId, request.newStatus());
    }

    /**
     * 공연 전체 목록을 조회
     * GET /concerts
     * -> Redis 캐시가 적용된 ConcertCacheService에서 페이징 기반으로 조회
     */
    @GetMapping
    public List<ConcertResponse> findAllConcerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return concertCacheService.getPagedConcertResponses(PageRequest.of(page, size));
    }

    /**
     * 공연 ID로 공연 단건을 조회
     * GET /concerts/{concertId}
     */
    @GetMapping("/{concertId}")
    public ConcertResponse getConcert(@PathVariable @Valid Long concertId) {
        Concert concert = concertService.getConcertById(concertId);
        return ConcertResponse.from(concert);
    }

    /**
     * 실시간 콘서트 랭킹 조회 (매진 시간 기준)
     * Redis ZSet 기반으로 오늘 매진된 콘서트를 빠른 순서로 정렬하여 조회합니다.
     * GET /concerts/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<ConcertRankingDetail>> getTopConcerts(
            @RequestParam(defaultValue = "3") int limit) {
        List<ConcertRankingDetail> result = concertRankingService.getTopConcertDetails(limit);
        return ResponseEntity.ok(result);
    }

    /**
     * 일일 콘서트 랭킹 조회 (매진 시간 기준)
     * 특정 날짜에 매진된 콘서트를 DB에서 조회합니다.
     *
     * GET /concerts/daily-ranking?date=2025-05-15
     */
    @GetMapping("/daily-ranking")
    public ResponseEntity<List<ConcertRankingResult>> getDailyRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyConcertRanking> rankings = dailyConcertRankingJpaRepository.findAllByRankingDate(date);
        List<ConcertRankingResult> result = rankings.stream()
                .map(r -> new ConcertRankingResult(r.getConcertId(), r.getSoldOutDurationMillis()))
                .toList();

        return ResponseEntity.ok(result);
    }
}
