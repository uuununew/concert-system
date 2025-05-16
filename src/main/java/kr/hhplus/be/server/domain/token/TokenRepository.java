package kr.hhplus.be.server.domain.token;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TokenRepository {

    void enqueue(String tokenId, long score);

    Optional<Integer> getWaitingPosition(String tokenId);

    void delete(String tokenId);

    void expireTokensBefore(long thresholdEpochSeconds);

    Set<String> findTopTokens(int limit);
}
