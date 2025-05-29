package kr.hhplus.be.server.infrastructure.token;

import java.time.LocalDateTime;

public record WaitingTokenRequestMessage(
        Long userId,
        Long concertId,
        LocalDateTime requestAt
) {}
