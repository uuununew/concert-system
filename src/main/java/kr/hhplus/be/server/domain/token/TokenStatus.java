package kr.hhplus.be.server.domain.token;

/**
 * QueueToken의 상태를 나타내는 Enum입니다.
 *
 * - WAITING: 대기 중 상태로, 활성화가 가능한 상태입니다.
 * - ACTIVE: 활성 상태이며, 다시 활성화할 수 없습니다.
 * - EXPIRED: 만료된 상태로, 활성화가 불가능합니다.
 *
 * 각 상태에 따라 `canActivate()`, `activate()` 메서드가 다르게 동작하며
 * 상태 전이 시 유효성 검증을 수행합니다.
 */
public enum TokenStatus {
    WAITING {
        @Override
        public boolean canActivate() {
            return true;
        }

        @Override
        public TokenStatus activate() {
            return ACTIVE;
        }
    },
    ACTIVE {
        @Override
        public boolean canActivate() {
            return false;
        }

        @Override
        public TokenStatus activate() {
            throw new IllegalStateException("이미 활성화된 토큰입니다.");
        }
    },
    EXPIRED {
        @Override
        public boolean canActivate() {
            return false;
        }

        @Override
        public TokenStatus activate() {
            throw new IllegalStateException("만료된 토큰은 활성화할 수 없습니다.");
        }
    };

    public abstract boolean canActivate();
    public abstract TokenStatus activate();
}
