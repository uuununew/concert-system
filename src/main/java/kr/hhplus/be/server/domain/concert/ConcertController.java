package kr.hhplus.be.server.domain.concert;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concerts")
public class ConcertController {

    private final ConcertService concertService;

    /**
     * 공연 전체 목록을 조회하는 API
     * GET /concerts
     * @return 공연 리스트 (ConcertResponse DTO로 변환)
     */
    @GetMapping
    public List<ConcertResponse> getConcerts(){
        return concertService.getConcertList().stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 공연 ID로 공연 단건을 조회하는 API
     * GET /concerts/{concertId}
     * @param concertId 조회할 공연 ID
     * @return ConcertResponse DTO
     */
    @GetMapping("/{concertId}")
    public ConcertResponse getConcert(@PathVariable Long concertId){
        Concert concert = concertService.getConcertById(concertId);
        return ConcertResponse.from(concert);
    }

    /**
     * 특정 공연의 전체 좌석 목록을 조회하는 API
     * GET /concerts/{concertId}/seats
     * @param concertId 대상 공연 ID
     * @return ConcertSeatResponse 리스트
     */
    @GetMapping("/{concertId}/seats")
    public List<ConcertSeatResponse> getAllSeats(@PathVariable Long concertId){
        return concertService.getSeats(concertId).stream()
                .map(ConcertSeatResponse::from)
                .collect(Collectors.toList());
    }

}
