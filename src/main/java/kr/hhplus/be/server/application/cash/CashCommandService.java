package kr.hhplus.be.server.application.cash;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @Transactional
    public void use(UseCashCommand command) {
        UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "캐시 정보가 없습니다."));

        userCash.use(command.getAmount());
        entityManager.flush();
        userCashRepository.save(userCash);

        cashHistoryRepository.save(CashHistory.use(userCash, command.getAmount()));
    }

    @Transactional
    public void charge(ChargeCashCommand command) {
        UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                .orElseGet(() -> new UserCash(command.getUserId(), command.getAmount()));

        if (userCash.getId() != null) {
            userCash.charge(command.getAmount());
        }
        entityManager.flush();

        userCashRepository.save(userCash);
        cashHistoryRepository.save(CashHistory.charge(userCash, command.getAmount()));
    }
}
