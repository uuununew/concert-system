package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueToken, Long> {
    Optional<QueueToken> findByUserId(Long userId);
    List<QueueToken> findAllByOrderByIssuedAtAsc();
    List<QueueToken> findAllByStatusOrderByIssuedAtAsc(TokenStatus status);
}
