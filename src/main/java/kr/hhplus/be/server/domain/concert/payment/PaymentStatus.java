package kr.hhplus.be.server.domain.concert.payment;

public enum PaymentStatus {
    READY,      // 결제 준비 상태
    PAID,       // 결제 완료
    CANCELED,   // 결제 취소
    FAILED      // 결제 실패
}
