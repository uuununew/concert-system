package kr.hhplus.be.server.domain.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.user.User;
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
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_seat_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ConcertSeat concertSeat;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자
    public Reservation(
            Long id,
            Long userId,
            ConcertSeat concertSeat,
            Long concertId,
            BigDecimal price,
            ReservationStatus status,
            LocalDateTime paidAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.concertSeat = concertSeat;
        this.concertId = concertId;
        this.price = price;
        this.status = status;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 정적 팩토리 메서드: 새 예약 생성
    public static Reservation create(User user, ConcertSeat seat, BigDecimal price) {
        return new Reservation(
                null,
                user.getId(),
                seat,
                seat.getConcert().getId(),
                price,
                ReservationStatus.RESERVED,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 결제 완료 처리
    public Reservation pay() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "RESERVED 상태일 때만 결제 가능합니다.");
        }
        return new Reservation(
                this.id,
                this.userId,
                this.concertSeat,
                this.concertId,
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
                this.concertSeat,
                this.concertId,
                this.price,
                ReservationStatus.CANCELED,
                this.paidAt,
                this.createdAt,
                LocalDateTime.now()
        );
    }
}
