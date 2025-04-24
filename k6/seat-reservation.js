import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import exec from 'k6/execution';

// 커스텀 메트릭 정의
const successCounter = new Counter('success_counter');
const failCounter = new Counter('fail_counter');
const successRate = new Rate('success_rate');

// k6 실행 옵션
export const options = {
    scenarios: {
        reservation_conflict_test: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            maxDuration: '30s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.1'],
        success_rate: ['rate>0.5'],
    },
};

// 테스트 대상 좌석과 사용자 목록
const FIXED_SEAT_ID = 1;  // 충돌 테스트를 위해 단일 좌석 고정
const reservationMap = Array.from({ length: 100 }, (_, i) => ({
    userId: i + 1,
    seatId: FIXED_SEAT_ID,
}));

const BASE_URL = 'http://localhost:8080';

export default function () {
    const index = exec.scenario.iterationInTest;
    const { userId, seatId } = reservationMap[index];

    const payload = JSON.stringify({
        userId: userId,
        concertSeatId: seatId,
        price: 10000
    });

    const headers = {
        'Content-Type': 'application/json',
    };

    const res = http.post(`${BASE_URL}/reservations`, payload, { headers });

    const passed = check(res, {
        'status is 201': (r) => r.status === 201,
        'status is 409 (conflict)': (r) => r.status === 409,
        'status is 400 (bad request)': (r) => r.status === 400,
    });

    if (res.status === 201) {
        successCounter.add(1);
        successRate.add(1);
        if (__ENV.DEBUG === 'true') {
            console.log(`✅ SUCCESS [userId=${userId}]`);
        }
    } else {
        failCounter.add(1);
        successRate.add(0);
        if (__ENV.DEBUG === 'true') {
            console.log(`❌ FAIL [userId=${userId}] → ${res.status}, ${res.body}`);
        }
    }
}