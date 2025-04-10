package kr.hhplus.be.server.presentation.concert.reservation;

import jakarta.validation.constraints.NotNull;

public record CancelReservationRequest(
        @NotNull(message = "reservationId는 필수입니다.")
        Long reservationId
) {}
