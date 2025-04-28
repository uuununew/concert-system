package kr.hhplus.be.server.domain.cash;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private LocalDateTime updatedAt;

    private CashHistory(UserCash userCash, CashHistoryType type, BigDecimal amount) {
        this.userCash = userCash;
        this.type = type;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static CashHistory charge(UserCash userCash, BigDecimal amount) {
        return new CashHistory(userCash, CashHistoryType.CHARGE, amount);
    }

    public static CashHistory use(UserCash userCash, BigDecimal amount) {
        return new CashHistory(userCash, CashHistoryType.USE, amount);
    }
}
