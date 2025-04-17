package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.CashHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashHistoryJpaRepository extends JpaRepository<CashHistory, Long> {
    List<CashHistory> findByUserCash_UserId(Long userId);
}
