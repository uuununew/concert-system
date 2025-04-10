package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.concert.ConcertSeatCommandService;
import kr.hhplus.be.server.application.concert.ConcertSeatQueryService;
import kr.hhplus.be.server.application.concert.CreateConcertSeatCommand;
import kr.hhplus.be.server.application.concert.UpdateConcertSeatCommand;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concerts/{concertId}/seats")
@RequiredArgsConstructor
public class ConcertSeatController {

    private final ConcertSeatCommandService concertSeatCommandService;
    private final ConcertSeatQueryService concertSeatQueryService;

    /**
     * [GET] /concerts/{concertId}/seats
     * 특정 공연의 모든 좌석 목록을 조회하는 API
     *
     * @param concertId 조회할 공연의 ID
     * @return 공연 좌석 목록을 반환하는 ConcertSeatResponse DTO 리스트
     */
    public List<ConcertSeatResponse> getSeats(@PathVariable Long concertId) {
        return concertSeatQueryService.getSeats(concertId).stream()
                .map(ConcertSeatResponse::from)
                .collect(Collectors.toList());
    }


    /**
     * [GET] /concerts/{concertId}/seats/available
     * 특정 공연의 예약 가능한 좌석 목록을 조회하는 API
     *
     * @param concertId 조회할 공연의 ID
     * @return 예약 가능한 좌석 목록을 반환하는 ConcertSeatResponse DTO 리스트
     */
    @GetMapping("/available")
    public List<ConcertSeatResponse> getAvailableSeats(@PathVariable Long concertId) {
        return concertSeatQueryService.getAvailableSeats(concertId).stream()
                .map(ConcertSeatResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * [POST] /concerts/{concertId}/seats
     * 좌석을 추가하는 API
     *
     * @param concertId 공연 ID
     * @param request 좌석 등록 요청 객체
     * @return 등록된 좌석의 정보
     */
    @PostMapping
    public ResponseEntity<ConcertSeatResponse> addSeat(
            @PathVariable Long concertId,
            @RequestBody @Valid CreateConcertSeatRequest request) {

        ConcertSeat seat = concertSeatCommandService.registerSeat(request.toCommand(concertId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConcertSeatResponse.from(seat));
    }

    /**
     * [PUT] /concerts/{concertId}/seats/{seatId}
     * 좌석 정보를 업데이트하는 API
     *
     * @param concertId 공연 ID
     * @param seatId 좌석 ID
     * @param request 좌석 수정 요청 객체
     * @return 수정된 좌석의 정보
     */
    @PutMapping("/{seatId}")
    public ResponseEntity<ConcertSeatResponse> updateSeat(
            @PathVariable Long concertId,
            @PathVariable Long seatId,
            @RequestBody @Valid UpdateConcertSeatRequest request) {

        ConcertSeat seat = concertSeatCommandService.updateSeat(seatId, request.toCommand());
        return ResponseEntity.ok(ConcertSeatResponse.from(seat));
    }
}
