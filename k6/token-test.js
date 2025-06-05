import http from 'k6/http';
import { check, sleep } from 'k6';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

export const options = {
  scenarios: {
    spike_test: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 2000,
      maxVUs: 5000,
      stages: [
        { duration: '10s', target: 1000 },
        { duration: '20s', target: 10000 },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.99'],
  },
};

const CONCERT_ID = __ENV.CONCERT_ID || 1;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export default function () {
  const userId = Math.floor(Math.random() * 1000000) + 1;
  const url = `${BASE_URL}/token?concertId=${CONCERT_ID}`;
  const headers = { 'X-USER-ID': userId.toString() };

  const res = http.post(url, null, { headers });

  // 성공(202) 또는 선착순 실패(409)는 OK로 처리
  check(res, {
    'status is 202 or 409': (r) => r.status === 202 || r.status === 409,
    'no 5xx error': (r) => r.status < 500
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    'summary.html': htmlReport(data, { title: '대기열 토큰 발급 Spike Test 결과' }),
  };
}