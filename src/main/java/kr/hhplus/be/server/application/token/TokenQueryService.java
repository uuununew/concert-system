package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.presentation.token.QueueTokenStatusResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenQueryService {

    private final TokenRepository tokenRepository;


    public QueueTokenStatusResponse getTokenStatus(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "토큰이 존재하지 않습니다."));

        int position = tokenRepository.getWaitingPosition(userId).orElse(0);

        return new QueueTokenStatusResponse(userId, token.getStatus(), position);
    }

    // 대기열 순서만 조회
    public Optional<Integer> getWaitingPosition(Long userId) {
        return tokenRepository.getWaitingPosition(userId);
    }
}
