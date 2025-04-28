package kr.hhplus.be.server.application.cash;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashCommandService {

    private final UserCashRepository userCashRepository;
    private final CashHistoryRepository cashHistoryRepository;
    private final EntityManager entityManager;

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

    @Transactional
    public void charge(ChargeCashCommand command) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                        .orElseGet(() -> new UserCash(command.getUserId(), command.getAmount()));

                userCash.charge(command.getAmount());
                userCashRepository.save(userCash);
                entityManager.flush();

                CashHistory history = CashHistory.charge(userCash, command.getAmount());
                cashHistoryRepository.save(history);

                return;
            }catch (OptimisticLockException e){
                attempts++;
                if (attempts >= 3) {
                    throw new CustomException(ErrorCode.CONCURRENT_REQUEST, "포인트 충전 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.");
                }
                log.warn("포인트 충전 중 충돌 발생, 재시도: {}회", attempts);
            }
        }
    }
}
