package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.lock.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import kr.hhplus.be.server.support.lock.RedisSimpleLock;
import kr.hhplus.be.server.support.exception.ErrorCode;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RedisSimpleLockAspect {

    private final RedisLockRepository redisLockRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("execution(@kr.hhplus.be.server.support.lock.RedisSimpleLock * *(..))")
    public Object applySimpleLock(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisSimpleLock redisSimpleLock = AnnotationUtils.findAnnotation(method, RedisSimpleLock.class);
        if (redisSimpleLock == null) {
            Method targetMethod = joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            redisSimpleLock = AnnotationUtils.findAnnotation(targetMethod, RedisSimpleLock.class);
        }

        if (redisSimpleLock == null) {
            throw new IllegalStateException("@RedisSimpleLock annotation not found!");
        }

        String key = parseKey(joinPoint, redisSimpleLock.key());
        long ttl = redisSimpleLock.ttl();

        Optional<String> lockValueOptional = redisLockRepository.acquireLock(key, ttl);

        if (lockValueOptional.isEmpty()) {
            throw new CustomException(ErrorCode.CONCURRENT_REQUEST, "락 획득 실패: " + key);
        }

        String lockValue = lockValueOptional.get();

        try {
            return joinPoint.proceed();
        } finally {
            redisLockRepository.releaseLock(key, lockValue);
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
