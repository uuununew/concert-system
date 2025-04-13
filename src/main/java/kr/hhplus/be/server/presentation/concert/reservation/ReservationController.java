package kr.hhplus.be.server.presentation.concert.reservation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.concert.reservation.ReservationCommandService;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
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
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestBody @Valid CreateReservationRequest request
    ) {
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
