package kr.hhplus.be.server.support.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisSpinLock {
    String key();
    long ttl() default 3000;
    long retryInterval() default 100;
    int retryCount() default 3;
}
