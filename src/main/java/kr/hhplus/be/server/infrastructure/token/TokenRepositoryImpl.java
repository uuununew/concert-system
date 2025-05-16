package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenRedisRepository redisRepository;

    @Override
    public void enqueue(String tokenId, long score) {
        redisRepository.enqueue(tokenId, score);
    }

    @Override
    public Optional<Integer> getWaitingPosition(String tokenId) {
        return redisRepository.getWaitingPosition(tokenId);
    }

    @Override
    public void delete(String tokenId) {
        redisRepository.remove(tokenId);
    }

    @Override
    public void expireTokensBefore(long thresholdEpochSeconds) {
        redisRepository.expireTokensBefore(thresholdEpochSeconds);
    }

    @Override
    public Set<String> findTopTokens(int limit) {
        return redisRepository.findTopTokens(limit);
    }
}