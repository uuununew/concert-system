package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.Reservation;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal amount;
    private LocalDateTime paidAt;

    // 정적 팩토리 메서드 생성
    public static Payment create(Reservation reservation, BigDecimal amount) {
        Payment payment = new Payment();
        payment.reservation = reservation;
        payment.amount = amount;
        payment.status = PaymentStatus.READY;
        return payment;
    }

    // 테스트용 팩토리 메서드 추가
    public static Payment withAll(
            Long id,
            Reservation reservation,
            PaymentStatus status,
            BigDecimal amount,
            LocalDateTime paidAt
    ) {
        Payment payment = new Payment();
        payment.id = id;
        payment.reservation = reservation;
        payment.status = status;
        payment.amount = amount;
        payment.paidAt = paidAt;
        return payment;
    }

    // 결제 처리
    public void pay() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "READY 상태일 때만 결제가 가능합니다.");
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 취소
    public void cancel() {
        if (this.status != PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제된 건만 취소할 수 있습니다.");
        }
        this.status = PaymentStatus.CANCELED;
        this.paidAt = null;
    }

    // 결제 실패
    public void fail() {
        if (this.status != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제가 실패 처리될 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.FAILED;
    }
}
