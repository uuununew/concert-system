package kr.hhplus.be.server.application.cash;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class UseCashCommand {

    private final Long userId;
    private final BigDecimal amount;

    public UseCashCommand(Long userId, BigDecimal amount) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId는 0보다 커야합니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }
        this.userId = userId;
        this.amount = amount;
    }

}