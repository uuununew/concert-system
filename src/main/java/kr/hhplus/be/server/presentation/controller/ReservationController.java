package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.reservation.SeatReservationRequest;
import kr.hhplus.be.server.presentation.dto.reservation.SeatReservationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservation", description = "좌석 예약 API")
public class ReservationController {
    @Operation(
            summary = "좌석 예약 요청",
            description = "콘서트 회차의 특정 좌석을 예약합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "예약 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SeatReservationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "콘서트 또는 회차 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<SeatReservationResponse> reserveSeat(@RequestBody SeatReservationRequest request) {
        SeatReservationResponse response = new SeatReservationResponse(
                9001L,
                LocalDateTime.now().toString()
        );
        return ResponseEntity.status(201).body(response);
    }
}
