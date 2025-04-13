package kr.hhplus.be.server.domain.concert.payment;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("결제 준비 상태"),
    PAID("결제 완료"),
    CANCELED("결제 취소"),
    FAILED("결제 실패");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
