package kr.hhplus.be.server.domain.concert.reservation;

public enum ReservationStatus {
    RESERVED,      // 임시 예약
    PAID,    // 결제 완료
    CANCELED     // 만료되거나 사용자가 취소
}
