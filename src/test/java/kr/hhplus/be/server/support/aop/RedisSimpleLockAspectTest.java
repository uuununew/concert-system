package kr.hhplus.be.server.support.aop;

import kr.hhplus.be.server.support.lock.RedisLockRepository;
import kr.hhplus.be.server.support.lock.RedisLockRepositoryImpl;
import kr.hhplus.be.server.support.lock.RedisSimpleLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RedisSimpleLockAspectTest {

    private RedisLockRepository redisLockRepository;
    private TestService testService;

    @BeforeEach
    void setUp() {
        redisLockRepository = Mockito.mock(RedisLockRepository.class);
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(true);

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
        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("락 획득 실패 시 예외가 발생한다")
    void fail() {
        when(redisLockRepository.acquireLock(anyString(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> testService.testMethod())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("요청하신 작업을 처리할 수 없습니다. (락 획득 실패");

        verify(redisLockRepository).acquireLock(anyString(), anyLong());
    }

    static class TestService {
        @RedisSimpleLock(key = "'testKey'", ttl = 1000L)
        public String testMethod() {
            return "success";
        }
    }
}