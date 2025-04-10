package kr.hhplus.be.server.presentation.cash;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 캐시 충전 또는 사용 후 잔액을 반환하는 응답 DTO
 */
public record CashResponse(
        BigDecimal balance
) {}