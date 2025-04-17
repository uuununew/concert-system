package kr.hhplus.be.server.support.config;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.infrastructure.token.TokenRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TokenConfig {

    private final TokenRepositoryImpl tokenRepositoryImpl;

    public TokenConfig(TokenRepositoryImpl tokenRepositoryImpl) {
        this.tokenRepositoryImpl = tokenRepositoryImpl;
    }

    @Bean
    public TokenRepository tokenRepository() {return tokenRepositoryImpl;}

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
