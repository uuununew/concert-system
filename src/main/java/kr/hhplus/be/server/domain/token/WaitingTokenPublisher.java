package kr.hhplus.be.server.domain.token;

import kr.hhplus.be.server.infrastructure.token.WaitingTokenRequestMessage;

public interface WaitingTokenPublisher {
    void publish(WaitingTokenRequestMessage message);
}
