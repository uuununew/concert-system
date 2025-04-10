package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ConcertSeatResponse {
    private Long id;
    private String seatNumber;
    private String section;
    private String row;
    private String grade;
    private BigDecimal price;
    private SeatStatus status;
    private LocalDateTime updatedAt;

    public ConcertSeatResponse(Long id, String seatNumber, String section, String row,
                               String grade, BigDecimal price, SeatStatus status, LocalDateTime updatedAt) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.section = section;
        this.row = row;
        this.grade = grade;
        this.price = price;
        this.status = status;
        this.updatedAt = updatedAt;
    }

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
