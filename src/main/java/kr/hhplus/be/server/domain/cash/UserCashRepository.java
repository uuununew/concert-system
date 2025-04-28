package kr.hhplus.be.server.domain.cash;

import java.util.Optional;

public interface UserCashRepository{
    Optional<UserCash> findByUserId(Long userId);
    UserCash save(UserCash userCash);

    //낙관적 락
    Optional<UserCash> findByUserIdForUpdate(Long userId);
}

