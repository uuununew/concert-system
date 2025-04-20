package kr.hhplus.be.server.domain.cash;

import java.util.List;

public interface CashHistoryRepository{
    List<CashHistory> findByUserId(Long userId);
    CashHistory save(CashHistory history);
}