package kr.hhplus.be.server.domain.cash;

import lombok.Getter;

/**
 * 사용자 cashhistory를 나타내는 enum입니다.
 * - CHARGE: 충전
 * - USE: 사용
 */
@Getter
public enum CashHistoryType {
    CHARGE("충전"),
    USE("사용");

    // 각 상태에 대한 설명을 저장하는 필드
    private final String description;

    // 생성자: enum 상수에 설명을 전달해 초기화
    CashHistoryType(String description) {
        this.description = description;
    }
}
