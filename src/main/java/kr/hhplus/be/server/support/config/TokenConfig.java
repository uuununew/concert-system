package kr.hhplus.be.server.support.config;

import kr.hhplus.be.server.application.concert.reservation.token.TokenCommandService;
import kr.hhplus.be.server.domain.concert.reservation.token.TokenManager;
import kr.hhplus.be.server.domain.concert.reservation.token.TokenRepository;
import kr.hhplus.be.server.infrastructure.concert.reservation.token.InMemoryTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TokenConfig {
    @Bean
    public TokenRepository tokenRepository() {
        return new InMemoryTokenRepository();
    }

    @Bean
    public TokenManager tokenManager(TokenRepository tokenRepository) {
        return new TokenManager(tokenRepository);
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public TokenCommandService tokenCommandService(TokenRepository tokenRepository,
                                                   TokenManager tokenManager,
                                                   Clock clock) {
        return new TokenCommandService(tokenRepository, tokenManager, clock, 5);
    }
}
