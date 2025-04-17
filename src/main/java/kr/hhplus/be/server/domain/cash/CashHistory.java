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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_cash_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserCash userCash;


    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private CashHistoryType type;

    private LocalDateTime createdAt;

    protected CashHistory() {}

    public CashHistory(UserCash userCash, BigDecimal amount, CashHistoryType type) {
        this.userCash = userCash;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public static CashHistory charge(UserCash userCash, BigDecimal amount) {
        return new CashHistory(userCash, amount, CashHistoryType.CHARGE);
    }

    public static CashHistory use(UserCash userCash, BigDecimal amount) {
        return new CashHistory(userCash, amount, CashHistoryType.USE);
    }
}
