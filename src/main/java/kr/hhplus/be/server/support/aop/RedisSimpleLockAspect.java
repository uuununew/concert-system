package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.lock.RedisLockRepository;
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
import kr.hhplus.be.server.support.lock.RedisSimpleLock;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RedisSimpleLockAspect {

    private final RedisLockRepository redisLockRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(kr.hhplus.be.server.support.lock.RedisSimpleLock)")
    public Object applySimpleLock(ProceedingJoinPoint joinPoint, RedisSimpleLock redisSimpleLock) throws Throwable {
        String key = parseKey(joinPoint, redisSimpleLock.key());
        long ttl = redisSimpleLock.ttl();

        boolean acquired = redisLockRepository.acquireLock(key, ttl);
        if (!acquired) {
            throw new IllegalStateException("Simple Redis 락 실패: " + key);
        }

        try {
            return joinPoint.proceed();
        } finally {
            redisLockRepository.releaseLock(key);
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
            throw new IllegalStateException("SpEL key expression 평가 결과가 null입니다: " + keyExpression);
        }
        return parsedKey;
    }
}
