package kr.hhplus.be.server.domain.concert.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    private final Long userId;

    private final Long reservationId;


    private final BigDecimal amount;

    private final LocalDateTime paidAt;

    public Payment(Long id, Long userId, Long reservationId, BigDecimal amount, LocalDateTime paidAt) {
        this.id = id;
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public static Payment create(Long userId, Long reservationId, BigDecimal amount, LocalDateTime paidAt) {
        return new Payment(null, userId, reservationId, amount, paidAt);
    }

    public Payment cancel() {
        return new Payment(
                this.id,
                this.userId,
                this.reservationId,
                this.amount,
                null // 결제 취소 → 결제일시 제거
        );
    }
}
