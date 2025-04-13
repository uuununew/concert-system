package kr.hhplus.be.server.support.scheduler;

import kr.hhplus.be.server.application.token.TokenCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenExpireScheduler {

    private final TokenCommandService tokenCommandService;

    public TokenExpireScheduler(TokenCommandService tokenCommandService) {
        this.tokenCommandService = tokenCommandService;
    }

    @Scheduled(fixedDelay = 1800000) // 30분마다 실행
    public void expireOverdueTokens() {
        tokenCommandService.expireOverdueTokens();
    }
}
