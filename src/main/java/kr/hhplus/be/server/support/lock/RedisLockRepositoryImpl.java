package kr.hhplus.be.server.support.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisLockRepositoryImpl implements RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 락 획득 시 고유한 UUID를 반환
     */
    @Override
    public Optional<String> acquireLock(String key, long ttlMillis) {
        String lockValue = UUID.randomUUID().toString();
        Boolean success = redisTemplate
                .opsForValue()
                .setIfAbsent(key, lockValue, Duration.ofMillis(ttlMillis));

        if (Boolean.TRUE.equals(success)) {
            log.debug("Redis 락 획득 성공 - key: {}, value: {}", key, lockValue);
            return Optional.of(lockValue);
        } else {
            log.warn("Redis 락 획득 실패 - key: {}", key);
            return Optional.empty();
        }
    }

    @Override
    public void releaseLock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        } else {
            log.warn("락 해제 실패 - key: {}, value 불일치", key);
        }
    }
}
