package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CashRepositoryConfig {

    @Bean
    public CashHistoryRepository cashHistoryRepository(CashHistoryJpaRepository jpaRepository) {
        return new CashHistoryRepository() {
            @Override
            public CashHistory save(CashHistory history) {
                return jpaRepository.save(history);
            }

            @Override
            public List<CashHistory> findByUserId(Long userId) {
                return jpaRepository.findAllByUserId(userId);
            }
        };
    }
}
