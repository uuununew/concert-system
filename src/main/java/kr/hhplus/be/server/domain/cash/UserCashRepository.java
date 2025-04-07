package kr.hhplus.be.server.domain.cash;

public interface UserCashRepository {
    UserCash findByUserId(Long userId);
}
