package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCommandService {

    private final TokenRepository tokenRepository;
    private final Clock clock;
    private final RedisTemplate<String, String> redisTemplate;

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

    @Transactional
    public void processWaitingToken(Long userId, Long concertId, LocalDateTime requestAt) {
        String dedupKey = "dedup:" + concertId + ":" + userId;

        // 1. 중복 여부 체크
        Boolean alreadyQueued = redisTemplate.hasKey(dedupKey);
        if (Boolean.TRUE.equals(alreadyQueued)) {
            log.warn("[중복] 대기열 중복 요청 차단: concertId={}, userId={}", concertId, userId);
            return;
        }

        // 2. 대기열 등록
        String tokenId = UUID.randomUUID().toString();
        long score = requestAt.toEpochSecond(ZoneOffset.UTC);
        String redisKey = "concert:" + concertId + ":" + tokenId;

        tokenRepository.enqueue(redisKey, score);
        log.info("[Redis] 대기열 저장 완료: concertId={}, userId={}, key={}", concertId, userId, redisKey);

        // 3. 중복 방지용 키 저장
        redisTemplate.opsForValue().set(dedupKey, "1", Duration.ofMinutes(30));
    }
}
