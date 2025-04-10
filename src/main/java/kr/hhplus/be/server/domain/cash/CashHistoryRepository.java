package kr.hhplus.be.server.domain.cash;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashHistoryRepository {
    CashHistory save(CashHistory cashHistory);

    List<CashHistory> findByUserId(Long userId);
}
