package kr.hhplus.be.server.domain.concert;

import lombok.Getter;

@Getter
public enum SeatStatus {
    AVAILABLE,
    RESERVED,
    SOLD_OUT
}
