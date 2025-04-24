package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueToken, Long> {
    Optional<QueueToken> findByUserId(Long userId);
    List<QueueToken> findAllByOrderByIssuedAtAsc();
    List<QueueToken> findAllByStatusOrderByIssuedAtAsc(TokenStatus status);
    List<QueueToken> findAllByStatusOrderByIssuedAt(TokenStatus status);

    @Query("SELECT t FROM QueueToken t WHERE t.status = :status AND t.issuedAt <= :before")
    List<QueueToken> findAllByStatusAndIssuedAtBefore(@Param("status") TokenStatus status, @Param("before") LocalDateTime before);
}
