package kr.hhplus.be.server.application.cash;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.presentation.cash.CashResponse;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kr.hhplus.be.server.domain.cash.CashHistory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashService {

    private final UserCashRepository userCashRepository;
    private final CashHistoryRepository cashHistoryRepository;

    /**
     * 유저의 캐시에 금액을 충전합니다.
     * - 유저가 존재하지 않으면 새로운 UserCash 엔티티를 생성합니다.
     * - 충전 금액은 Command에서 유효성 검사를 거친 상태로 전달됩니다.
     * - 최대 충전 한도를 초과하면 예외가 발생합니다.
     */
    @Transactional
    public CashResult charge(ChargeCashCommand command) {
        UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                .orElseGet(() -> new UserCash(command.getUserId(), BigDecimal.ZERO));

        userCash.charge(command.getAmount());
        userCashRepository.save(userCash);

        //충전이력 저장
        cashHistoryRepository.save(CashHistory.use(userCash, command.getAmount()));

        return new CashResult(userCash.getAmount());

    }

    /**
     * 유저의 캐시를 사용합니다.
     * - 유저가 존재하지 않으면 예외를 발생시킵니다.
     * - 사용 금액은 Command에서 유효성 검사를 거친 상태로 전달됩니다.
     * - 잔액보다 많은 금액을 사용하려고 하면 예외가 발생합니다.
     */
    @Transactional
    public CashResult use(UseCashCommand command) {
        UserCash userCash = userCashRepository.findByUserId(command.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        userCash.use(command.getAmount());
        userCashRepository.save(userCash);

        //사용이력 저장
        cashHistoryRepository.save(CashHistory.use(userCash, command.getAmount()));

        return new CashResult(userCash.getAmount());
    }
    /**
     * 특정 유저의 캐시 이력을 조회합니다.
     *
     * @param userId 유저 ID
     * @return 해당 유저의 모든 캐시 이력 리스트
     */
    public List<CashHistory> getHistories(Long userId) {
        return cashHistoryRepository.findByUserId(userId);
    }
}
