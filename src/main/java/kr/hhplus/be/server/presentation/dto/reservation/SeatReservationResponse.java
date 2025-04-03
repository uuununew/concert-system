package kr.hhplus.be.server.presentation.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 예약 응답 DTO")
public record SeatReservationResponse(

        @Schema(description = "예약 ID", example = "9001")
        Long reservationId,

        @Schema(description = "예약 시각", example = "2025-05-01T13:00:00")
        String reservedAt

) {}