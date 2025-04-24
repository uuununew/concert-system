package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCashRepositoryImpl implements UserCashRepository {

    private final UserCashJpaRepository userCashJpaRepository;

    @Override
    public Optional<UserCash> findByUserId(Long userId) {
        return userCashJpaRepository.findByUserId(userId);
    }

    @Override
    public UserCash save(UserCash userCash) {
        return userCashJpaRepository.save(userCash);
    }

    @Override
    public Optional<UserCash> findByUserIdWithOptimistic(Long userId) {
        return userCashJpaRepository.findByUserIdWithOptimistic(userId);
    }
}
