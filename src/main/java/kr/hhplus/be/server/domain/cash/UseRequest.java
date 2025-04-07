package kr.hhplus.be.server.domain.cash;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UseRequest {
    private Long userId;
    private BigDecimal amount;
}
