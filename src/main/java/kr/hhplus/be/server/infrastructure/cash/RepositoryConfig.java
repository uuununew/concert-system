package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RepositoryConfig {

    @Bean
    public CashHistoryRepository cashHistoryRepository(CashHistoryJpaRepository jpaRepository) {
        return new CashHistoryRepositoryImpl(jpaRepository);
    }
}
