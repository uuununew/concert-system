package kr.hhplus.be.server.support.lock;

public interface RedisLockRepository {

    boolean acquireLock(String key, long ttl);
    void releaseLock(String key);
}
