package kr.hhplus.be.server.domain.cash;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashService {

    private final UserCashRepository userCashRepository;

    public BigDecimal charge(Long userId, BigDecimal amount){
        UserCash userCash = userCashRepository.findByUserId(userId);
        userCash.charge(amount);
        return userCash.getAmount();
    }

}
