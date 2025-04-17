package kr.hhplus.be.server.presentation.cash;

import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 캐시 이력 응답 DTO
 */
public record CashHistoryResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        CashHistoryType type,
        LocalDateTime createdAt
) {
    public static CashHistoryResponse from(CashHistory history) {
        return new CashHistoryResponse(
                history.getId(),
                history.getUserCash().getUserId(),
                history.getAmount(),
                history.getType(),
                history.getCreatedAt()
        );
    }
}
