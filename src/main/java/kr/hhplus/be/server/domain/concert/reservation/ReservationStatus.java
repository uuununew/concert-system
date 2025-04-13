package kr.hhplus.be.server.domain.concert.reservation;

import lombok.Getter;
    /**
     * 예약 상태를 나타내는 enum입니다.
     * - RESERVED: 임시 예약 상태
     * - PAID: 결제 완료 상태
     * - CANCELED: 만료되거나 사용자가 취소한 상태
     */
    @Getter
    public enum ReservationStatus {
        RESERVED("임시 예약"),
        PAID("결제 완료"),
        CANCELED("만료 또는 사용자 취소");

        // 각 상태에 대한 설명을 저장하는 필드
        private final String description;

        // 생성자: enum 상수에 설명을 전달해 초기화
        ReservationStatus(String description) {
            this.description = description;
        }
}
