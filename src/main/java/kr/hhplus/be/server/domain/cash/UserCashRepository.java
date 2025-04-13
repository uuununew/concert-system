package kr.hhplus.be.server.domain.cash;

import java.util.Optional;

public interface UserCashRepository {
    Optional<UserCash> findByUserId(Long userId);
    void save(UserCash userCash);
}
