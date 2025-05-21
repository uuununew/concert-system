package kr.hhplus.be.server.domain.concert.ranking;

import java.time.LocalDateTime;

public record ConcertSoldOutEvent(Long concertId, LocalDateTime concertOpenTime, LocalDateTime soldOutTime) {}