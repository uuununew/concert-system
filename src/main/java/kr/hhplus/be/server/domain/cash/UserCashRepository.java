package kr.hhplus.be.server.domain.cash;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCashRepository{
    Optional<UserCash> findByUserId(Long userId);
    UserCash save(UserCash userCash);
}

