package kr.hhplus.be.server.support.scheduler;

import kr.hhplus.be.server.application.token.TokenCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenScheduler {

    private final TokenCommandService tokenCommandService;

    // TTL 만료 처리: 30분마다 실행
    @Scheduled(fixedDelay = 1800000)
    public void expireOverdueTokens() {
        tokenCommandService.expireOverdueTokens();
    }

    // 15분마다 상위 1000명 자동 활성화
    @Scheduled(fixedDelay = 900000)  // 15분 = 900,000ms
    public void activateTopTokens() {
        tokenCommandService.activateEligibleTokens(1000);
    }
}
