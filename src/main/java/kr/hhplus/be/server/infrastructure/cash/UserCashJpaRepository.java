package kr.hhplus.be.server.infrastructure.cash;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.cash.UserCash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCashJpaRepository extends JpaRepository<UserCash, Long> {
    Optional<UserCash> findByUserId(Long userId);

    //낙관적 락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT uc FROM UserCash uc WHERE uc.userId = :userId")
    Optional<UserCash> findByUserIdForUpdate(@Param("userId") Long userId);
}