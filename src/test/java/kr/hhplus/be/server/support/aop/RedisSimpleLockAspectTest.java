package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.support.lock.RedisLockRepository;
import kr.hhplus.be.server.support.lock.RedisSimpleLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RedisSimpleLockAspectTest {

    private RedisLockRepository redisLockRepository;
    private TestService testService;

    @BeforeEach
    void setUp() {
        redisLockRepository = Mockito.mock(RedisLockRepository.class);
        when(redisLockRepository.acquireLock(anyString(), anyLong()))
                .thenReturn(Optional.of("mock-lock-value"));

        TestService targetService = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(targetService);
        factory.addAspect(new RedisSimpleLockAspect(redisLockRepository));

        testService = factory.getProxy();
    }

    @Test
    @DisplayName("락 획득 성공 시 비즈니스 로직이 실행된다")
    void success() {
        String result = testService.testMethod();

        verify(redisLockRepository).acquireLock(anyString(), anyLong());
        verify(redisLockRepository).releaseLock(anyString(), anyString());
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("락 획득 실패 시 예외가 발생한다")
    void fail() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(Optional.empty());

         assertThatThrownBy(() -> testService.testMethod())
                .isInstanceOf(RuntimeException.class)
                 .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                         .isEqualTo(ErrorCode.CONCURRENT_REQUEST));

        verify(redisLockRepository).acquireLock(anyString(), anyLong());
    }

    @Test
    @DisplayName("Redis 접속 실패 시 예외가 발생한다")
    void should_throw_exception_when_redis_down() {
        RedisLockRepository failingRepository = Mockito.mock(RedisLockRepository.class);
        when(failingRepository.acquireLock(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Redis 접속 실패"));

        AspectJProxyFactory factory = new AspectJProxyFactory(new TestService());
        factory.addAspect(new RedisSimpleLockAspect(failingRepository));
        TestService proxyService = factory.getProxy();

        assertThatThrownBy(proxyService::testMethod)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Redis 접속 실패");
    }

    @Test
    @DisplayName("SpEL 파싱 실패 시 fallback key가 사용된다")
    void fallbackKeyOnSpelParseFailure() {
        String result = testService.methodWithInvalidSpel();

        verify(redisLockRepository).acquireLock(startsWith("fallback-"), anyLong());
        verify(redisLockRepository).releaseLock(startsWith("fallback-"), anyString());
        assertThat(result).isEqualTo("fallback");
    }


    static class TestService {

        @RedisSimpleLock(key = "'testKey'", ttl = 1000L)
        public String testMethod() {
            return "success";
        }

        @RedisSimpleLock(key = "#invalidParam", ttl = 1000L)
        public String methodWithInvalidSpel() {
            return "fallback";
        }
    }
}