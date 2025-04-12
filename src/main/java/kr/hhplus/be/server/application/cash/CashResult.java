package kr.hhplus.be.server.application.cash;

import java.math.BigDecimal;

// application 내부 전용 Cash 결과 객체
public class CashResult {

    private final BigDecimal amount;

    public CashResult(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
