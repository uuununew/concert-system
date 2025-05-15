package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenCommandService {

    private final TokenRepository tokenRepository;
    private final TokenManager tokenManager;
    private final Clock clock;

    @Value("${token.expire.minutes:15}")
    private final int expireMinutes;

    @Transactional
    public String issue(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        long score = LocalDateTime.now(clock).toEpochSecond(ZoneOffset.UTC);
        tokenRepository.enqueue(tokenId, score);
        return tokenId;
    }

    public void activateEligibleTokens(int limit) {
        Set<String> topTokens = tokenRepository.findTopTokens(limit);
        for (String tokenId : topTokens) {
            tokenRepository.delete(tokenId);
        }
    }

    public void expireOverdueTokens() {
        long threshold = LocalDateTime.now(clock)
                .minusMinutes(expireMinutes)
                .toEpochSecond(ZoneOffset.UTC);
        tokenRepository.expireTokensBefore(threshold);
    }

    public void complete(String tokenId) {
        tokenRepository.delete(tokenId);
    }

    public Optional<Integer> status(String tokenId) {
        return tokenRepository.getWaitingPosition(tokenId);
    }
}
