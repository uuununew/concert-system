package kr.hhplus.be.server.domain.cash;

import jakarta.persistence.*;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
public class UserCash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Version // 낙관적 락 적용
    private Long version;

    protected UserCash() {
    }

    private static final BigDecimal MAX_CASH_LIMIT = BigDecimal.valueOf(1_000_000); // 100만원 한도

    /**
     * 실제 서비스용 생성자
     */
    public UserCash(Long userId, BigDecimal initialAmount) {
        this.userId = userId;
        this.amount = initialAmount;
    }

    /**
     * 캐시를 충전한다.
     * 최대 충전 한도(100만원)를 초과하면 예외 발생
     */
    public void charge(BigDecimal amount) {
        if (this.amount.add(amount).compareTo(MAX_CASH_LIMIT) > 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "최대 충전 가능 금액을 초과했습니다.");
        }
        this.amount = this.amount.add(amount);
    }

    /**
     * 캐시를 사용한다.
     * 잔액이 부족하면 예외 발생
     */
    public void use(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "0원 이하 금액은 사용할 수 없습니다.");
        }
        if (this.amount.compareTo(amount) < 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "잔액이 부족합니다.");
        }
        this.amount = this.amount.subtract(amount);
    }
}
