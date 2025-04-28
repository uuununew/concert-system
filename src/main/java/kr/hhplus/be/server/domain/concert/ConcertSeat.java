package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Concert concert;

    private String seatNumber;
    private String section;

    @Column(name = "seat_row")
    private String row;

    private String grade;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public ConcertSeat(
            Long id,
            Concert concert,
            String seatNumber,
            String section,
            String row,
            String grade,
            BigDecimal price,
            SeatStatus status,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.concert = concert;
        this.seatNumber = seatNumber;
        this.section = section;
        this.row = row;
        this.grade = grade;
        this.price = price;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    /**
     * 비즈니스 로직용 생성 메서드
     * 초기 상태는 항상 AVAILABLE, 업데이트 시간은 현재 시간으로 설정
     */
    public static ConcertSeat of(Concert concert, String seatNumber, String section, String row, String grade, BigDecimal price) {
        ConcertSeat seat = new ConcertSeat();
        seat.concert = concert;
        seat.seatNumber = seatNumber;
        seat.section = section;
        seat.row = row;
        seat.grade = grade;
        seat.price = price;
        seat.status = SeatStatus.AVAILABLE;
        seat.updatedAt = LocalDateTime.now();
        return seat;
    }


    /**
     * 좌석 정보를 업데이트하는 메서드
     * 필드 값들이 변경되며 업데이트 시간이 갱신됩니다.
     */
    public void updateSeatInfo(String seatNumber, String section, String row, String grade, BigDecimal price) {
        this.seatNumber = seatNumber;
        this.section = section;
        this.row = row;
        this.grade = grade;
        this.price = price;
        this.updatedAt = LocalDateTime.now(); // 변경 시마다 업데이트 시간 갱신
    }

    /**
     * 테스트용 팩토리 메서드
     */
    public static ConcertSeat withAll(Long id, Concert concert, String seatNumber, String section, String row,
                                      String grade, BigDecimal price, SeatStatus status, LocalDateTime updatedAt) {
        ConcertSeat seat = new ConcertSeat();
        seat.id = id;
        seat.concert = concert;
        seat.seatNumber = seatNumber;
        seat.section = section;
        seat.row = row;
        seat.grade = grade;
        seat.price = price;
        seat.status = status;
        seat.updatedAt = updatedAt;
        return seat;
    }

    /**
     * 예약 가능한 상태인지 확인
     */
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    /**
     * 좌석 상태를 RESERVED로 전환
     */
    public void reserve() {
        if (!isAvailable()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "예약할 수 없는 좌석입니다.");
        }
        this.status = SeatStatus.RESERVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좌석 상태를 SOLD_OUT으로 전환
     */
    public void markSoldOut() {
        if (this.status != SeatStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "좌석 상태가 RESERVED일 때만 결제 완료로 전환할 수 있습니다.");
        }
        this.status = SeatStatus.SOLD_OUT;
        this.updatedAt = LocalDateTime.now();
    }

}
