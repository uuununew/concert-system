package kr.hhplus.be.server.domain.cash;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CashResponse {
    private final BigDecimal balance;

    public CashResponse(BigDecimal balance) {
        this.balance = balance;
    }

}
