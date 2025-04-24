import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import exec from 'k6/execution';

// 커스텀 메트릭 정의
const successCounter = new Counter('success_counter');
const failCounter = new Counter('fail_counter');
const successRate = new Rate('success_rate');
const requestDuration = new Trend('request_duration');

// 부하 설정
export const options = {
  vus: 20,
  iterations: 100,
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95%는 500ms 미만
    http_req_failed: ['rate<0.1'],     // 실패율 10% 이하
    success_rate: ['rate>0.9'],        // 성공률 90% 이상
  },
};

// 테스트 대상 API
const BASE_URL = 'http://localhost:8080';
const ENDPOINT = '/cash/use';
const TEST_USER_ID = 1;

export default function () {
    const payload = JSON.stringify({
        userId: TEST_USER_ID,
        amount: 1000
    });

    const headers = {
        'Content-Type': 'application/json',
    };

  const start = new Date().getTime();
      const res = http.post(`${BASE_URL}${ENDPOINT}`, payload, { headers });
      const duration = new Date().getTime() - start;

      const isSuccess = check(res, {
          'status is 200': (r) => r.status === 200,
      });

      if (isSuccess) {
          successCounter.add(1);
          successRate.add(1);
      } else {
          failCounter.add(1);
          successRate.add(0);
          console.log(`[❌] FAIL - status: ${res.status}, body: ${res.body}`);
      }

      requestDuration.add(duration);

      if (exec.scenario.iterationInTest % 10 === 0) {
          console.log(`✅ VU ${exec.vu.idInTest}, Iteration ${exec.scenario.iterationInTest}, Duration: ${duration}ms`);
      }
}