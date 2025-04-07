package kr.hhplus.be.server.domain.cash;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ChargeRequest {
    private Long userId;
    private BigDecimal amount;

}
