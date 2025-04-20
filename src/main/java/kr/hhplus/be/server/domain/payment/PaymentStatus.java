package kr.hhplus.be.server.domain.payment;

import lombok.Getter;

public enum PaymentStatus {
    READY,
    PAID,
    CANCELED,
    FAILED
}
