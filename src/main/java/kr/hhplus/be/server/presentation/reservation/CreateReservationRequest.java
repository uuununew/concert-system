package kr.hhplus.be.server.presentation.reservation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;

import java.math.BigDecimal;

public record CreateReservationRequest(
        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        @NotNull(message = "concertSeatId는 필수입니다.")
        Long concertSeatId,

        @NotNull(message = "price는 필수입니다.")
        @DecimalMin(value = "1", inclusive = false, message = "price는 0보다 커야 합니다.")
        BigDecimal price,

        @NotBlank(message = "tokenId는 필수입니다.")
                String tokenId
) {
    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(
                tokenId,
                userId,
                concertSeatId,
                price
        );
    }
}
