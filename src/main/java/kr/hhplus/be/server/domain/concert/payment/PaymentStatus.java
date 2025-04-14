package kr.hhplus.be.server.domain.concert.payment;

import lombok.Getter;

public enum PaymentStatus {
    READY,
    PAID,
    CANCELED,
    FAILED
}
