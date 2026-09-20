/*
 * Local Compose load lab — not a production capacity test.
 *
 * Gateway rate limits (one client IP): 10 login/min, 60 other req/min.
 * Login once in setup(); unique Idempotency-Key per booking.
 * Contended scenario hammers one (showtime, seat); expect one 201 and the rest 409.
 * Compose runs a single replica, so this measures lock/unique/saga behavior, not horizontal scale.
 *
 *   make up && make load
 *   BASE_URL=http://localhost:8090 k6 run scripts/load/booking.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const BASE = __ENV.BASE_URL || 'http://localhost:8090';
const EMAIL = __ENV.EMAIL || 'user@cinema.local';
const PASSWORD = __ENV.PASSWORD || 'User@123';
const SHOWTIME_ID = Number(__ENV.SHOWTIME_ID || 1);
const CONTENDED_SEAT = __ENV.CONTENDED_SEAT || 'K1';

const sagaLatency = new Trend('saga_confirm_ms');
const bookingCreated = new Counter('booking_created');
const bookingConflict = new Counter('booking_conflict');

export const options = {
  thresholds: {
    http_req_failed: ['rate<0.3'],
    saga_confirm_ms: ['p(95)<15000'],
  },
  scenarios: {
    browse: {
      executor: 'constant-arrival-rate',
      rate: 1,
      timeUnit: '1s',
      duration: '25s',
      preAllocatedVUs: 2,
      exec: 'browse',
    },
    contended: {
      executor: 'per-vu-iterations',
      vus: 12,
      iterations: 1,
      startTime: '2s',
      exec: 'contendSeat',
    },
    saga: {
      executor: 'per-vu-iterations',
      vus: 4,
      iterations: 1,
      startTime: '4s',
      exec: 'bookAndPollSaga',
    },
  },
};

export function setup() {
  const res = http.post(
    `${BASE}/api/v1/auth/login`,
    JSON.stringify({ email: EMAIL, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(res, { 'login 200': (r) => r.status === 200 });
  const body = res.json();
  return { token: body.accessToken };
}

function authHeaders(token, extra) {
  return Object.assign(
    {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
      'X-Correlation-Id': `k6-${__VU}-${__ITER}-${Date.now()}`,
    },
    extra || {},
  );
}

export function browse() {
  const movies = http.get(`${BASE}/api/v1/movies?size=5`);
  const theaters = http.get(`${BASE}/api/v1/theaters`);
  const showtimes = http.get(`${BASE}/api/v1/showtimes`);
  check(movies, { 'movies 200 or 429': (r) => r.status === 200 || r.status === 429 });
  check(theaters, { 'theaters 200 or 429': (r) => r.status === 200 || r.status === 429 });
  check(showtimes, { 'showtimes 200 or 429': (r) => r.status === 200 || r.status === 429 });
}

export function contendSeat(data) {
  const res = http.post(
    `${BASE}/api/v1/bookings`,
    JSON.stringify({ showtimeId: SHOWTIME_ID, seats: [CONTENDED_SEAT] }),
    {
      headers: authHeaders(data.token, {
        'Idempotency-Key': `contend-${__VU}-${__ITER}-${Date.now()}-${Math.random()}`,
      }),
    },
  );
  const ok = check(res, {
    'contended 201 or 409': (r) => r.status === 201 || r.status === 409,
  });
  if (res.status === 201) {
    bookingCreated.add(1);
  } else if (res.status === 409) {
    bookingConflict.add(1);
  } else if (!ok) {
    console.error(`contend unexpected ${res.status}: ${res.body}`);
  }
}

export function bookAndPollSaga(data) {
  const seat = `S${__VU}`;
  const create = http.post(
    `${BASE}/api/v1/bookings`,
    JSON.stringify({ showtimeId: SHOWTIME_ID, seats: [seat] }),
    {
      headers: authHeaders(data.token, {
        'Idempotency-Key': `saga-${__VU}-${Date.now()}-${Math.random()}`,
      }),
    },
  );
  check(create, { 'saga booking 201': (r) => r.status === 201 });
  if (create.status !== 201) {
    console.error(`saga create ${create.status}: ${create.body}`);
    return;
  }
  const bookingId = create.json('id');
  const started = Date.now();
  let status = create.json('status');
  for (let i = 0; i < 20 && status === 'PENDING'; i += 1) {
    sleep(0.5);
    const poll = http.get(`${BASE}/api/v1/bookings/${bookingId}`, {
      headers: authHeaders(data.token),
    });
    if (poll.status === 200) {
      status = poll.json('status');
    }
  }
  sagaLatency.add(Date.now() - started);
  check(null, {
    'saga reached CONFIRMED or FAILED': () => status === 'CONFIRMED' || status === 'FAILED',
  });
}
