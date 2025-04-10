package kr.hhplus.be.server.domain.cash;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
public class CashHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CashHistoryType type;

    private LocalDateTime createdAt;

    protected CashHistory() {}

    public CashHistory(Long userId, BigDecimal amount, CashHistoryType type) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public static CashHistory charge(Long userId, BigDecimal amount) {
        return new CashHistory(userId, amount, CashHistoryType.CHARGE);
    }

    public static CashHistory use(Long userId, BigDecimal amount) {
        return new CashHistory(userId, amount, CashHistoryType.USE);
    }
}
