package kr.hhplus.be.server.domain.concert.reservation;

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
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    private final Long userId;
    private final Long concertSeatId;
    private final BigDecimal price;

    @Enumerated(EnumType.STRING)
    private final ReservationStatus status;

    private final LocalDateTime paidAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // 생성자
    public Reservation(
            Long id,
            Long userId,
            Long concertSeatId,
            BigDecimal price,
            ReservationStatus status,
            LocalDateTime paidAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.concertSeatId = concertSeatId;
        this.price = price;
        this.status = status;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 정적 팩토리 메서드: 새 예약 생성
    public static Reservation create(
            Long userId,
            Long concertSeatId,
            BigDecimal price
    ) {
        return new Reservation(
                null,
                userId,
                concertSeatId,
                price,
                ReservationStatus.RESERVED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 결제 완료 처리
    public Reservation markPaid() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "RESERVED 상태일 때만 결제 가능합니다.");
        }
        return new Reservation(
                this.id,
                this.userId,
                this.concertSeatId,
                this.price,
                ReservationStatus.PAID,
                LocalDateTime.now(),
                this.createdAt,
                LocalDateTime.now()
        );
    }

    // 취소 처리
    public Reservation cancel() {
        if (this.status == ReservationStatus.PAID) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제 완료된 예약은 취소할 수 없습니다.");
        }
        return new Reservation(
                this.id,
                this.userId,
                this.concertSeatId,
                this.price,
                ReservationStatus.CANCELED,
                this.paidAt,
                this.createdAt,
                LocalDateTime.now()
        );
    }
}
