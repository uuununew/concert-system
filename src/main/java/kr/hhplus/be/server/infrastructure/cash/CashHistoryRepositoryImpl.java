package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Repository
public class CashHistoryRepositoryImpl implements CashHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CashHistoryRepositoryImpl.class);

    private final CashHistoryJpaRepository cashHistoryJpaRepository;

    public CashHistoryRepositoryImpl(CashHistoryJpaRepository cashHistoryJpaRepository) {
        if (cashHistoryJpaRepository == null) {
            log.error("❌ CashHistoryJpaRepository is NULL in constructor!");
        } else {
            log.info("✅ CashHistoryJpaRepository successfully injected");
        }

        this.cashHistoryJpaRepository = cashHistoryJpaRepository;
    }

    @Override
    @Transactional
    public CashHistory save(CashHistory history) {
        return cashHistoryJpaRepository.save(history);
    }

    @Override
    public List<CashHistory> findByUserId(Long userId) {
        return cashHistoryJpaRepository.findByUserId(userId);
    }
}
