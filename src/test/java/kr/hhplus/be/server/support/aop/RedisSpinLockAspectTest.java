package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.lock.RedisLockRepository;
import kr.hhplus.be.server.support.lock.RedisSpinLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;

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
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(true);

        TestService targetService = new TestService();

        AspectJProxyFactory factory = new AspectJProxyFactory(targetService);
        factory.addAspect(new RedisSpinLockAspect(redisLockRepository));

        testService = factory.getProxy();
    }

    @Test
    @DisplayName("락 획득에 성공하면 메서드가 정상 실행된다")
    void should_execute_method_when_lock_acquired_successfully() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(true);

        String result = testService.someMethod("value");

        verify(redisLockRepository, times(1)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, times(1)).releaseLock(anyString());
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("락 획득에 실패하면 예외가 발생한다")
    void should_throw_exception_when_lock_acquisition_fails() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> testService.someMethod("value"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Redis 락 획득 실패");

        // 실제 구현에서는 retryCount 횟수만큼 시도합니다
        // 단일 시도를 사용하는 경우 times(1)로 변경하세요
        verify(redisLockRepository, times(3)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, never()).releaseLock(anyString());
    }

    @Test
    @DisplayName("락 획득 시도 중 timeout 발생 시 예외가 발생한다")
    void should_throw_timeout_exception_when_lock_cannot_be_acquired_in_time() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> testService.someMethod("value"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Redis 락 획득 실패");

        verify(redisLockRepository, times(3)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, never()).releaseLock(anyString());
    }

    @Test
    @DisplayName("메서드 실행 중 예외 발생 시 락이 해제된다")
    void should_release_lock_even_when_exception_occurs_in_method() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> testService.exceptionMethod("value"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예외 발생");

        verify(redisLockRepository, times(1)).acquireLock(anyString(), anyLong());
        verify(redisLockRepository, times(1)).releaseLock(anyString());
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
    }
}
