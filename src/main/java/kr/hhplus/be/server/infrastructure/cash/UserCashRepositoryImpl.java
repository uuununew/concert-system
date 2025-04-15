package kr.hhplus.be.server.infrastructure.cash;

import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserCashRepositoryImpl implements UserCashRepository {

    private final UserCashJpaRepository userCashJpaRepository;

    public UserCashRepositoryImpl(UserCashJpaRepository jpaCashRepository) {
        this.userCashJpaRepository = jpaCashRepository;
    }

    @Override
    public Optional<UserCash> findByUserId(Long userId) {
        return userCashJpaRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserCash save(UserCash userCash) {
        return userCashJpaRepository.save(userCash);
    }
}
