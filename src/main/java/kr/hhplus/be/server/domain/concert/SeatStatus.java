package kr.hhplus.be.server.domain.concert;

import lombok.Getter;

@Getter
public enum SeatStatus {
    AVAILABLE("예약 가능"),
    RESERVED("임시 예약됨"),
    SOLD_OUT("결제 완료됨");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }
}
