package kr.hhplus.be.server.application.cash;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.support.lock.RedisSpinLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashCommandService {

    private final UserCashRepository userCashRepository;
    private final CashHistoryRepository cashHistoryRepository;
    private final EntityManager entityManager;

    @RedisSpinLock(key = "'cash-lock:' + #command.userId", ttl = 500, retryInterval = 50, retryCount = 5)
    @Transactional
    public void use(UseCashCommand command) {
        try{
            UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "캐시 정보가 없습니다."));

            userCash.use(command.getAmount());
            userCashRepository.save(userCash);
            entityManager.flush();

            CashHistory history = CashHistory.use(userCash, command.getAmount());
            cashHistoryRepository.save(history);
        }catch (OptimisticLockException e){
            throw new CustomException(ErrorCode.CONCURRENT_REQUEST, "포인트 사용 중 충돌이 발생했습니다. 다시 시도해주세요");
        }

    }

    @RedisSpinLock(key = "'cash-lock:' + #command.userId", ttl = 500, retryInterval = 50, retryCount = 5)
    @Transactional
    public void charge(ChargeCashCommand command) {
        try {
            UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                    .orElseGet(() -> new UserCash(command.getUserId(), BigDecimal.ZERO));

            userCash.charge(command.getAmount());

            // 테스트 환경에서 락이 오래 유지되도록 의도적으로 지연
            Thread.sleep(2000);

            entityManager.flush();
            userCashRepository.save(userCash);

            cashHistoryRepository.save(CashHistory.charge(userCash, command.getAmount()));
        } catch (OptimisticLockException e) {
            throw new CustomException(ErrorCode.CONCURRENT_REQUEST, "포인트 충전 중 충돌이 발생했습니다.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "요청 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
