package kr.hhplus.be.server.application.cash.concurrency;

import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.cash.UseCashCommand;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class CashConcurrencyTest extends TestContainerConfig {

    @Autowired
    private CashService cashService;

    @DisplayName("동시에 캐시를 사용할 경우 하나만 성공해야 한다.")
    @Test
    void useCash_concurrent_fail_on_insufficient_balance() throws InterruptedException{
        //given
        Long userId = 1L;
        cashService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(1000)));

        int threadCount = 5; //5개의 스레드가 동시에 캐시 사용
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        //when
        ConcurrencyTestExecutor.run(threadCount, () ->{
            try{
                cashService.use(new UseCashCommand(userId, BigDecimal.valueOf(600)));
            }catch (Throwable t){
                exceptions.add(t); //실패 케이스 저장
            }
        });

        //then : 성공 요청 수는 1이 되어야 한다
        long successCount = threadCount - exceptions.size();
        assertThat(successCount).isEqualTo(1);
    }
}
