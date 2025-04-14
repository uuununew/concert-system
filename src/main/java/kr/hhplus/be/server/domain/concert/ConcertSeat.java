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

    private Long concertId;

    private String seatNumber;
    private String section;

    @Column(name = "seat_row")
    private String row;

    private String grade;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private LocalDateTime updatedAt;

    public ConcertSeat(
            Long id,
            Long concertId,
            String seatNumber,
            String section,
            String row,
            String grade,
            BigDecimal price,
            SeatStatus status,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.concertId = concertId;
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
    public static ConcertSeat of(Long concertId, String seatNumber, String section, String row, String grade, BigDecimal price) {
        return new ConcertSeat(
                null,
                concertId,
                seatNumber,
                section,
                row,
                grade,
                price,
                SeatStatus.AVAILABLE,
                LocalDateTime.now()
        );
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
    public static ConcertSeat withAll(Long id, Long concertId, String seatNumber, String section, String row,
                                      String grade, BigDecimal price, SeatStatus status, LocalDateTime updatedAt) {
        ConcertSeat seat = new ConcertSeat();
        seat.id = id;
        seat.concertId = concertId;
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
