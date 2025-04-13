package kr.hhplus.be.server.application.cash;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ChargeCashCommand{

    private final Long userId;
    private final BigDecimal amount;

    public ChargeCashCommand(Long userId, BigDecimal amount) {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "충전 금액은 0보다 커야 합니다");
        }
        this.userId = userId;
        this.amount = amount;
    }

}

