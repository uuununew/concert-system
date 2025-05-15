package kr.hhplus.be.server.presentation.reservation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.domain.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationCommandService reservationCommandService;

    /**
     * [POST] /reservations
     * 좌석 예약 API
     * - 유효한 토큰이 있는 사용자만 예약 가능
     * - 예약 성공 시 토큰은 사용 처리됨(EXPIRED)
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestBody @Valid CreateReservationRequest request, HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");  // Interceptor가 세팅한 값
        Reservation reservation = reservationCommandService.reserve(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    /**
     * [PUT] /cancel
     * 취소 API
     */
    @PutMapping("/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @RequestBody @Valid CancelReservationRequest request
    ) {
        Reservation reservation = reservationCommandService.cancel(request.reservationId());
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
