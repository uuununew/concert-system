package kr.hhplus.be.server.domain.concert;

import lombok.Getter;

@Getter
public enum ConcertStatus {
    READY("공연 준비 중"),
    OPENED("예약 가능"),
    CLOSED("공연 종료"),
    CANCELED("공연 취소");

    private final String description;

    ConcertStatus(String description) {
        this.description = description;
    }
}
