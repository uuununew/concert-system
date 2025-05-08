package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.lock.RedisLockRepository;
import kr.hhplus.be.server.support.lock.RedisSpinLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RedisSpinLockAspect {

    private final RedisLockRepository redisLockRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(kr.hhplus.be.server.support.lock.RedisSpinLock)")
    public Object applySpinLock(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisSpinLock redisSpinLock = method.getAnnotation(RedisSpinLock.class);

        String key = parseKey(joinPoint, redisSpinLock.key());
        long ttl = redisSpinLock.ttl();
        long retryInterval = redisSpinLock.retryInterval();
        int retryCount = redisSpinLock.retryCount();

        boolean lockAcquired = false;

        try {
            for (int i = 0; i < retryCount; i++) {
                lockAcquired = redisLockRepository.acquireLock(key, ttl);
                if (lockAcquired) {
                    log.debug("[RedisSpinLockAspect] Redis 락 획득 성공 - key: {}", key);
                    break;
                }
                Thread.sleep(retryInterval);
            }

            if (!lockAcquired) {
                throw new IllegalStateException("Redis 락 획득 실패: " + key);
            }
            return joinPoint.proceed();

        } catch (Throwable t) {
            log.error("[RedisSpinLockAspect] 예외 발생 - key: {} - {}", key, t.getMessage(), t);
            throw t;

        } finally {
            if (lockAcquired) {
                redisLockRepository.releaseLock(key);
            }
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String parsedKey = parser.parseExpression(keyExpression).getValue(context, String.class);
        if (parsedKey == null) {
            throw new IllegalStateException("SpEL key expression이 null입니다: " + keyExpression);
        }
        return parsedKey;
    }
}
