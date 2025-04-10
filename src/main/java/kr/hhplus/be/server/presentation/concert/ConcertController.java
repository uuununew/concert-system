package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.presentation.concert.CreateConcertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertService concertService;

    /**
     * [POST] /concerts
     * 콘서트 등록 API
     */
    @PostMapping
    public ConcertResponse registerConcert(@RequestBody @Valid CreateConcertRequest request) {
        CreateConcertCommand command = request.toCommand();
        Concert concert = concertService.registerConcert(command);
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
     */
    @GetMapping
    public List<ConcertResponse> findAllConcerts() {
        return concertService.getConcertList().stream()
                .map(ConcertResponse::from)
                .toList();
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
