package kr.hhplus.be.server.presentation.concert.reservation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.concert.reservation.CreateReservationCommand;

import java.math.BigDecimal;

public record CreateReservationRequest(
        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        @NotNull(message = "concertSeatId는 필수입니다.")
        Long concertSeatId,

        @NotNull(message = "price는 필수입니다.")
        @DecimalMin(value = "1", inclusive = false, message = "price는 0보다 커야 합니다.")
        BigDecimal price
) {
    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(userId, concertSeatId, price);
    }
}
