import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 1. НАСТРОЙКА СТУПЕНЕЙ НАГРУЗКИ (до 360 VU)
export const options = {
  stages: [
    { duration: '5m', target: 120 }, 
    { duration: '10s', target: 132 }, { duration: '10s', target: 144 },
    { duration: '10s', target: 156 }, { duration: '10s', target: 168 },
    { duration: '10s', target: 180 }, { duration: '10s', target: 192 },
    { duration: '10s', target: 204 }, { duration: '10s', target: 216 },
    { duration: '10s', target: 228 }, { duration: '10s', target: 240 },
    { duration: '10s', target: 264 }, { duration: '10s', target: 288 },
    { duration: '10s', target: 312 }, { duration: '10s', target: 336 },
    { duration: '10s', target: 360 }, // Максимальный пик
    { duration: '30s', target: 0 },  
  ],
  thresholds: { http_req_failed: ['rate<0.25'] },
};

const postPasteDuration = new Trend('http_req_duration_post_paste', true);
const getPasteDuration = new Trend('http_req_duration_get_paste', true);
const postPasteFailRate = new Rate('http_req_failed_post_paste');
const getPasteFailRate = new Rate('http_req_failed_get_paste');

const BASE_URL = 'http://localhost:80/api';
const TEST_JWT = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzNjIwNDE4NzYiLCJyb2xlIjoiVVNFUiIsImZpcnN0TmFtZSI6ItCd0LjQutC40YLQsCIsImxhc3ROYW1lIjoi0J_QvtC_0L7QsiIsImlzcyI6InBvbHlfcGFzdGUiLCJpYXQiOjE3ODE2MzY0MDUsImV4cCI6MTc4MTcyMjgwNX0.cE2it2KO7mmVi9xFCoT0VUDHVUNJPNxsDCHKXIzOO1U';
const KNOWN_SHORT_URLS = ['1538a20f'];

const heavyPayload = JSON.stringify({
  content: 'A'.repeat(1024 * 100), syntax: 'Plain Text', expirationTime: null, burnAfterRead: false, visibility: 'PUBLIC', password: null
});

export default function () {
  const params = { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${TEST_JWT}` } };
  const randomRoll = Math.random();

  if (randomRoll < 0.04) {
    const res = http.post(`${BASE_URL}/pastes/create`, heavyPayload, params);
    postPasteDuration.add(res.timings.duration);
    postPasteFailRate.add(res.status !== 200 && res.status !== 201);
    check(res, { 'POST 200/201': (r) => r.status === 200 || r.status === 201 });
  } else {
    const randomUrl = KNOWN_SHORT_URLS[Math.floor(Math.random() * KNOWN_SHORT_URLS.length)];
    const res = http.get(`${BASE_URL}/pastes/${randomUrl}`, params);
    getPasteDuration.add(res.timings.duration);
    getPasteFailRate.add(res.status !== 200);
    check(res, { 'GET 200': (r) => r.status === 200 });
  }
  sleep(1);
}