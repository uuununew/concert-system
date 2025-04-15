package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
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
    private Long id;

    private Long userId;
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal amount;
    private LocalDateTime paidAt;

    public Payment(
            Long id,
            Long userId,
            Long reservationId,
            PaymentStatus status,
            BigDecimal amount,
            LocalDateTime paidAt
    ) {
        this.id = id;
        this.userId = userId;
        this.reservationId = reservationId;
        this.status = status;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public static Payment create(Long userId, Long reservationId, BigDecimal amount) {
        return new Payment(null, userId, reservationId, PaymentStatus.READY, amount, null);
    }

    public Payment pay() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "READY 상태일 때만 결제가 가능합니다.");
        }
        return new Payment(this.id, this.userId, this.reservationId, PaymentStatus.PAID, this.amount, LocalDateTime.now());
    }

    public Payment cancel() {
        if (this.status != PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제된 건만 취소할 수 있습니다.");
        }
        return new Payment(this.id, this.userId, this.reservationId, PaymentStatus.CANCELED, this.amount, null);
    }

    public Payment fail() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제가 실패 처리될 수 없는 상태입니다.");
        }
        return new Payment(this.id, this.userId, this.reservationId, PaymentStatus.FAILED, this.amount, this.paidAt);
    }
}
