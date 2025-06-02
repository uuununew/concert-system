package kr.hhplus.be.server.support.config;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.infrastructure.token.TokenRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Clock;

@Configuration
@RequiredArgsConstructor
public class TokenConfig {

    private final TokenRepositoryImpl tokenRepositoryImpl;
    private final RedisTemplate<String, String> redisTemplate;

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
    public TokenCommandService tokenCommandService(
            TokenManager tokenManager,
            Clock clock
    ) {
        return new TokenCommandService(tokenRepositoryImpl, clock, redisTemplate, 5);
    }
}
