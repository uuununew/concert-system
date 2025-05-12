package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.support.lock.RedisLockRepository;
import kr.hhplus.be.server.support.lock.RedisSpinLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class RedisSpinLockAspectTest {

    private RedisLockRepository redisLockRepository;
    private TestService testService;

    @BeforeEach
    void setUp() {
        redisLockRepository = Mockito.mock(RedisLockRepository.class);
        when(redisLockRepository.acquireLock(anyString(), anyLong()))
                .thenReturn(Optional.of("mock-lock-value"));

        TestService targetService = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(targetService);
        factory.addAspect(new RedisSpinLockAspect(redisLockRepository));

        testService = factory.getProxy();
    }

    @Test
    @DisplayName("락 획득에 성공하면 메서드가 정상 실행된다")
    void should_execute_method_when_lock_acquired_successfully() {
        String result = testService.someMethod("value");

        verify(redisLockRepository, times(1)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, times(1)).releaseLock(anyString(), anyString());
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("락 획득에 실패하면 예외가 발생한다")
    void should_throw_exception_when_lock_acquisition_fails() {
        when(redisLockRepository.acquireLock(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> testService.someMethod("value"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.CONCURRENT_REQUEST.getMessage());

        // 실제 구현에서는 retryCount 횟수만큼 시도합니다
        // 단일 시도를 사용하는 경우 times(1)로 변경
        verify(redisLockRepository, times(3)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, never()).releaseLock(any(), any());
    }

    @Test
    @DisplayName("SpEL 파싱 실패 시 fallback key로 처리되고 정상 실행된다")
    void should_fallback_when_spel_key_is_invalid() {
        String result = testService.methodWithInvalidSpel("ignored");

        verify(redisLockRepository).acquireLock(startsWith("fallback-"), anyLong());
        verify(redisLockRepository).releaseLock(anyString(), anyString());
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("락 획득 시도 중 timeout 발생 시 예외가 발생한다")
    void should_throw_timeout_exception_when_lock_cannot_be_acquired_in_time() {
        when(redisLockRepository.acquireLock(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> testService.someMethod("value"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    CustomException ce = (CustomException) e;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.CONCURRENT_REQUEST);
                });

        verify(redisLockRepository, times(3)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, never()).releaseLock(any(), any());
    }

    @Test
    @DisplayName("메서드 실행 중 예외 발생 시 락이 해제된다")
    void should_release_lock_even_when_exception_occurs_in_method() {
        when(redisLockRepository.acquireLock(anyString(), anyLong()))
                .thenReturn(Optional.of("mock-lock-value"));

        assertThatThrownBy(() -> testService.exceptionMethod("value"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예외 발생");

        verify(redisLockRepository, times(1)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, times(1)).releaseLock(anyString(), anyString());
    }

    static class TestService {
        @RedisSpinLock(key = "#param", ttl = 1000, retryInterval = 100, retryCount = 3)
        public String someMethod(String param) {
            return "success";
        }

        @RedisSpinLock(key = "#param", ttl = 1000, retryInterval = 100, retryCount = 3)
        public String exceptionMethod(String param) {
            throw new RuntimeException("예외 발생");
        }

        @RedisSpinLock(key = "#invalidParam", ttl = 1000, retryInterval = 100, retryCount = 3)
        public String methodWithInvalidSpel(String param) {
            return "success";
        }
    }
}
