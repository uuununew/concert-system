package kr.hhplus.be.server.presentation.cash;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UseCashRequest(
        @NotNull(message = "userId는 null일 수 없습니다.")
        Long userId,

        @NotNull(message = "사용 금액은 null일 수 없습니다.")
        @DecimalMin(value = "1", inclusive = true, message = "사용 금액은 1원 이상이어야 합니다.")
        BigDecimal amount
) { }
