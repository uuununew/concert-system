package kr.hhplus.be.server.domain.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
public class ConcertSeat {

    private final Long id;
    private final Long concertId;

    private final String seatNumber;
    private final String section;
    private final String row;
    private final String grade;
    private final BigDecimal price;
    private final SeatStatus status;
    private final LocalDateTime updatedAt;

    /**
     * 좌석이 예약 가능한 상태인지 확인
     */
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    /**
     * 좌석이 임시 예약된 상태인지 확인
     */
    public boolean isReserved() {
        return this.status == SeatStatus.RESERVED;
    }

    /**
     * 좌석이 결제 완료 상태인지 확인
     */
    public boolean isSoldOut() {
        return this.status == SeatStatus.SOLD_OUT;
    }

    /**
     * 좌석을 RESERVED 상태로 변경
     * - 현재 상태가 AVAILABLE일 때만 가능
     * - 새로운 객체로 상태 변경 (불변 객체 패턴)
     */
    public ConcertSeat reserve() {
        if (!isAvailable()) {
            throw new IllegalStateException("예약할 수 없는 좌석입니다.");
        }
        return new ConcertSeat(id, concertId, seatNumber, section, row, grade, price, SeatStatus.RESERVED, LocalDateTime.now());
    }

    /**
     * 좌석을 SOLD_OUT 상태로 변경
     * - 현재 상태가 RESERVED일 때만 가능
     * - 새로운 객체로 상태 변경 (불변 객체 패턴)
     */
    public ConcertSeat markSoldOut() {
        if (!isReserved()) {
            throw new IllegalStateException("선택된 좌석만 판매 완료 상태로 바꿀 수 있습니다.");
        }
        return new ConcertSeat(id, concertId, seatNumber, section, row, grade, price, SeatStatus.SOLD_OUT, LocalDateTime.now());
    }

}
