package kr.hhplus.be.server.domain.concert;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ConcertSeatResponse {
    private final Long id;
    private final String seatNumber;
    private final String section;
    private final String row;
    private final String grade;
    private final BigDecimal price;
    private final SeatStatus status;
    private final LocalDateTime updatedAt;

    public ConcertSeatResponse(Long id, String seatNumber, String section, String row, String grade,
                               BigDecimal price, SeatStatus status, LocalDateTime updatedAt) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.section = section;
        this.row = row;
        this.grade = grade;
        this.price = price;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // 도메인 객체 ConcertSeat를 ConcertSeatResponse DTO로 변환
    public static ConcertSeatResponse from(ConcertSeat seat) {
        return new ConcertSeatResponse(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getSection(),
                seat.getRow(),
                seat.getGrade(),
                seat.getPrice(),
                seat.getStatus(),
                seat.getUpdatedAt()
        );
    }
}
