package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 콘서트 응답 DTO
 */
@Getter
public class ConcertResponse implements Serializable {

    private final Long id;
    private final String title;
    private final int round;
    private final ConcertStatus status;
    private final LocalDateTime concertDateTime;

    public ConcertResponse(Long id, String title, int round, ConcertStatus status, LocalDateTime concertDateTime) {
        this.id = id;
        this.title = title;
        this.round = round;
        this.status = status;
        this.concertDateTime = concertDateTime;
    }

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getRound(),
                concert.getStatus(),
                concert.getConcertDateTime()
        );
    }
}
