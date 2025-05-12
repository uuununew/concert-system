package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.concert.ConcertCacheService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.domain.concert.Concert;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertCacheService concertCacheService;

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
}
