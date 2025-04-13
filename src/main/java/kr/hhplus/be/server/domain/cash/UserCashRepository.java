package kr.hhplus.be.server.domain.cash;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCashRepository extends JpaRepository<UserCash, Long> {
    Optional<UserCash> findByUserId(Long userId);
}

