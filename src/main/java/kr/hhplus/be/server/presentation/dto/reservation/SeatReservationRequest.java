package kr.hhplus.be.server.presentation.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 예약 요청 DTO")
public class SeatReservationRequest {

    @Schema(description = "콘서트 ID", example = "1", required = true)
    private Long concertId;

    @Schema(description = "회차 ID", example = "101", required = true)
    private Long scheduleId;

    @Schema(description = "좌석 번호", example = "A-10", required = true)
    private String seatNumber;

    @Schema(description = "유저 ID", example = "42", required = true)
    private Long userId;

    public SeatReservationRequest() {}

    public SeatReservationRequest(Long concertId, Long scheduleId, String seatNumber, Long userId) {
        this.concertId = concertId;
        this.scheduleId = scheduleId;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }

    public Long getConcertId() { return concertId; }
    public Long getScheduleId() { return scheduleId; }
    public String getSeatNumber() { return seatNumber; }
    public Long getUserId() { return userId; }
}
