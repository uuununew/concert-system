package kr.hhplus.be.server.infrastructure.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
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
        String luaScript = """
        redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2])
        redis.call('SET', KEYS[2] .. ARGV[2], '1')
        redis.call('EXPIRE', KEYS[2] .. ARGV[2], ARGV[3])
        return true
    """;

        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Boolean.class),
                List.of(QUEUE_KEY, TTL_PREFIX),
                score + "", tokenId, String.valueOf(10 * 60)  // TTL 초 단위
        );
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

        String luaScript = """
            for i = 1, #ARGV do
                local ttlKey = KEYS[2] .. ARGV[i]
                local exists = redis.call('EXISTS', ttlKey)
                if exists == 0 then
                    redis.call('ZREM', KEYS[1], ARGV[i])
                end
            end
            return true
        """;

        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Boolean.class),
                List.of(QUEUE_KEY, TTL_PREFIX),
                candidates.toArray()
        );
    }
}
