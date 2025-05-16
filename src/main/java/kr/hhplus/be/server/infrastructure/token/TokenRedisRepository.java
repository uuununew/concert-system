package kr.hhplus.be.server.infrastructure.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String QUEUE_KEY = "reservation:queue";
    private static final String TTL_PREFIX = "reservation:token:EXPIRE:";

    // 대기열에 토큰 추가
    public void enqueue(String tokenId, long score) {
        redisTemplate.opsForZSet().add(QUEUE_KEY, tokenId, score);

        String ttlKey = TTL_PREFIX + tokenId;
        redisTemplate.opsForValue().set(ttlKey, "1", Duration.ofMinutes(10));
    }

    // 대기열 순위 조회
    public Optional<Integer> getWaitingPosition(String tokenId) {
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, tokenId);
        return rank == null ? Optional.empty() : Optional.of(rank.intValue());
    }

    // 대기열 전체 토큰 ID
    public Set<String> findAll() {
        return redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
    }

    // 토큰 제거
    public void remove(String tokenId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, tokenId);
        redisTemplate.delete(TTL_PREFIX + tokenId);
    }

    public Set<String> findTopTokens(int limit) {
        return redisTemplate.opsForZSet().range(QUEUE_KEY, 0, limit - 1);
    }

    // TTL이 만료된 토큰 제거
    public void expireTokensBefore(long threshold) {
        Set<String> candidates = redisTemplate.opsForZSet().rangeByScore(QUEUE_KEY, 0, threshold);
        if (candidates == null || candidates.isEmpty()) return;

        for (String tokenId : candidates) {
            String ttlKey = TTL_PREFIX + tokenId;
            Boolean exists = redisTemplate.hasKey(ttlKey);
            if (Boolean.FALSE.equals(exists)) {
                redisTemplate.opsForZSet().remove(QUEUE_KEY, tokenId);
            }
        }
    }
}
