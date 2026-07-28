import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 1. НАСТРОЙКА БАЗОВОЙ НАГРУЗКИ ПО МЕТОДИЧКЕ (120 VU на 15 минут)
export const options = {
  stages: [
    { duration: '5m', target: 120 },  // Плавный разгон до 120
    { duration: '15m', target: 120 }, // Удержание плато 15 минут (как в отчете)
    { duration: '2m', target: 0 },    // Спуск
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'], // Для базы порог 10%
  },
};

const postPasteDuration = new Trend('http_req_duration_post_paste', true);
const getPasteDuration = new Trend('http_req_duration_get_paste', true);
const postPasteFailRate = new Rate('http_req_failed_post_paste');
const getPasteFailRate = new Rate('http_req_failed_get_paste');

const BASE_URL = 'http://localhost:80/api';
const TEST_JWT = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzNjIwNDE4NzYiLCJyb2xlIjoiVVNFUiIsImZpcnN0TmFtZSI6ItCd0LjQutC40YLQsCIsImxhc3ROYW1lIjoi0J_QvtC_0L7QsiIsImlzcyI6InBvbHlfcGFzdGUiLCJpYXQiOjE3ODE2MzY0MDUsImV4cCI6MTc4MTcyMjgwNX0.cE2it2KO7mmVi9xFCoT0VUDHVUNJPNxsDCHKXIzOO1U';
const KNOWN_SHORT_URLS = ['1538a20f'];

// Генерируем 100КБ ОДИН РАЗ, чтобы не убить память
const heavyPayload = JSON.stringify({
  content: 'A'.repeat(1024 * 100),
  syntax: 'Plain Text',
  expirationTime: null,
  burnAfterRead: false,
  visibility: 'PUBLIC',
  password: null
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