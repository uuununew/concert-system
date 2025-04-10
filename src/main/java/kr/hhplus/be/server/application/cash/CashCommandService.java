package kr.hhplus.be.server.application.cash;

import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashCommandService {

    private final UserCashRepository userCashRepository;

    /**
     * 유저의 캐시를 차감합니다.
     * - 존재하지 않으면 예외 발생
     * - 캐시 도메인에서 유효성 검증 수행
     */
    public void use(UseCashCommand command) {
        UserCash cash = userCashRepository.findByUserId(command.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "캐시 정보가 없습니다."));

        cash.use(command.getAmount());
        userCashRepository.save(cash);
    }
}
