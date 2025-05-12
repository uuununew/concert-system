package kr.hhplus.be.server.support.lock;

import java.util.Optional;

public interface RedisLockRepository {

    Optional<String> acquireLock(String key, long ttl);
    void releaseLock(String key, String value);
}
