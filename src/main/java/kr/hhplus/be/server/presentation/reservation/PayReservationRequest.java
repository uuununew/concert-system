package kr.hhplus.be.server.presentation.reservation;

import jakarta.validation.constraints.NotNull;

public record PayReservationRequest(
        @NotNull(message = "예약 ID는 필수입니다.")
        Long reservationId
) {
}
