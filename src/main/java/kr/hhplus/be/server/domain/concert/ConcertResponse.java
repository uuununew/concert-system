package kr.hhplus.be.server.domain.concert;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertResponse {

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
