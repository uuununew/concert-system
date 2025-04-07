package kr.hhplus.be.server.domain.cash;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UserCash {

    private final Long userId;
    private BigDecimal amount;
    private static final BigDecimal MAX_CASH_LIMIT = new BigDecimal("1_000_000"); //100만원 한도

    public UserCash(Long userId, BigDecimal initialAmount){
        if(userId == null) throw new IllegalArgumentException("userID는 null일 수 없습니다.");
        if(initialAmount == null || initialAmount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("초기 금액은 0 이상이어야 합니다.");
        }
        this.userId = userId;
        this.amount = initialAmount;
    }

    public void charge(BigDecimal value){
        validate(value);

        if (this.amount.add(value).compareTo(MAX_CASH_LIMIT) > 0) {
            throw new IllegalArgumentException("최대 충전 가능 금액을 초과했습니다.");
        }
        this.amount = this.amount.add(value);
    }

    public void use(BigDecimal value){
        validate(value);
        if(this.amount.compareTo(value) < 0){
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        this.amount = this.amount.subtract(value);
    }

    private void validate(BigDecimal value){
        if(value == null || value.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("금액은 0보다 커야합니다.");
        }
    }
}
