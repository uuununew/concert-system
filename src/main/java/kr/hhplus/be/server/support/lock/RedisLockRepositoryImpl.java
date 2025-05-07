package kr.hhplus.be.server.support.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisLockRepositoryImpl implements RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_VALUE = "LOCKED";

    @Override
    public boolean acquireLock(String key, long ttlMillis) {
        Boolean success = redisTemplate
                .opsForValue()
                .setIfAbsent(key, LOCK_VALUE, Duration.ofMillis(ttlMillis));

        if (Boolean.TRUE.equals(success)) {
            log.debug("Redis 락 획득 성공 - key: {}", key);
        } else {
            log.warn("Redis 락 획득 실패 - key: {}", key);
        }
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
