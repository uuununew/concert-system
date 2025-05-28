package kr.hhplus.be.server.infrastructure.concert.ranking;

import kr.hhplus.be.server.domain.concert.ConcertSeatCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConcertSeatCountRedisRepository implements ConcertSeatCountRepository {

    private static final String REMAIN_KEY_PREFIX = "concert:remain-seats:";
    private final StringRedisTemplate redisTemplate;

    private String getKey(Long concertId) {
        return REMAIN_KEY_PREFIX + concertId;
    }

    @Override
    public long decrementRemainCount(Long concertId) {
        Long result = redisTemplate.opsForValue().decrement(getKey(concertId));
        if (result == null) throw new IllegalStateException("Redis decrement 실패");
        return result;
    }

    @Override
    public long incrementRemainCount(Long concertId) {
        Long result = redisTemplate.opsForValue().increment(getKey(concertId));
        if (result == null) throw new IllegalStateException("Redis increment 실패");
        return result;
    }

    @Override
    public long getRemainCount(Long concertId) {
        String value = redisTemplate.opsForValue().get(getKey(concertId));
        return (value != null) ? Long.parseLong(value) : 0L;
    }
}
