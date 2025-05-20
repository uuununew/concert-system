package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.presentation.token.QueueTokenStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenQueryService {

    private final TokenRepository tokenRepository;

    public QueueTokenStatusResponse getTokenStatus(String tokenId) {
        Optional<Integer> positionOpt = tokenRepository.getWaitingPosition(tokenId);

        // ZSet에 없으면 EXPIRED 상태로 판단
        if (positionOpt.isEmpty()) {
            return new QueueTokenStatusResponse(
                    tokenId,
                    null,
                    TokenStatus.EXPIRED,
                    null
            );
        }

        int position = positionOpt.get();
        TokenStatus status = (position == 0) ? TokenStatus.ACTIVE : TokenStatus.WAITING;

        return new QueueTokenStatusResponse(
                tokenId,
                null,
                status,
                position
        );
    }

    // 대기열 순서만 조회
    public Optional<Integer> getWaitingPosition(String tokenId) {
        return tokenRepository.getWaitingPosition(tokenId);
    }
}

