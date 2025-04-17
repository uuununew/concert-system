package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.UserCash;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCashJpaRepository extends JpaRepository<UserCash, Long> {
    Optional<UserCash> findByUserId(Long userId);
}