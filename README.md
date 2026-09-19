# Movie Booking Platform

[![CI Pipeline](https://github.com/longhang2004/movie-booking-microservice/actions/workflows/ci.yml/badge.svg)](https://github.com/longhang2004/movie-booking-microservice/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-KRaft-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

Distributed cinema ticketing system. Clients hit a Spring Cloud Gateway; downstream services own their data, coordinate seat inventory with Redis + a unique constraint, and settle payment asynchronously through a transactional outbox and Kafka.

---

## Runtime topology

```
                         HTTP
                           │
                           ▼
              ┌─────────────────────────┐
              │  api-gateway :8090      │
              │  JWT resource server    │
              │  Redis INCR rate limit  │
              │  X-Correlation-Id       │
              │  lb:// via Eureka       │
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │  discovery-server :8761 │
              └─────────────────────────┘

  auth-service :8096          movie-service :8091
  HS256 access + refresh      paginated search
  RBAC USER | ADMIN           Redis cache (movies)

  theater-service :8092       showtime-service :8093
  theaters / rooms            Feign → movie, theater
                              Resilience4j CB + retry

  booking-service :8094       payment-service :8095
  hexagonal application       Kafka consumer
  Redis SET NX seat hold      idempotent charge / bookingId
  outbox → payment-requested  → payment-completed

  PostgreSQL 16 (db-per-service)   Redis 7   Kafka 3.8 KRaft
  Zipkin :9411   Prometheus :9090   Grafana :3000
```

All public traffic is `/api/v1/**`. Gateway rewrites to the service path (`/bookings`, `/movies`, …) and load-balances with `lb://{spring.application.name}`. OpenAPI docs are proxied as `/{service}/v3/api-docs` **before** the catch-all `/{service}/**` routes so Swagger UI at the gateway can aggregate definitions.

---

## Request path

1. Gateway issues/forwards `X-Correlation-Id` (also used as Micrometer `traceId` context).
2. Redis key `rl:{clientIp}`: `INCR` + 60s TTL, **60 req/min**. `/actuator/**` and OpenAPI paths are excluded. Over limit → `429`.
3. JWT is required except for:
   - `POST /api/v1/auth/**`
   - `GET` catalog: movies, theaters, showtimes
   - actuator / swagger
4. Resource servers decode HS256 (`app.jwt.secret`), map claim `roles` → `ROLE_*`. Catalog **writes** need `ADMIN`. Booking/payment APIs need an authenticated user; booking reads are owner-or-admin.

Demo seed accounts (non-`test` profile):

| Email | Password | Role |
|-------|----------|------|
| `admin@cinema.local` | `Admin@123` | `ADMIN` |
| `user@cinema.local` | `User@123` | `USER` |

---

## Bounded contexts

| Process | Port | Persistence | Sync deps | Async |
|---------|------|-------------|-----------|--------|
| `auth-service` | 8096 | `auth_db` | — | — |
| `movie-service` | 8091 | `movie_db` + Redis cache | — | — |
| `theater-service` | 8092 | `theater_db` | — | — |
| `showtime-service` | 8093 | `showtime_db` | Feign movie + theater | — |
| `booking-service` | 8094 | `booking_db` + Redis locks | Feign showtime | produce `payment-requested`, consume `payment-completed` |
| `payment-service` | 8095 | `payment_db` | — | consume `payment-requested`, produce `payment-completed` |

Each service runs Flyway (`ddl-auto: validate`, `open-in-view: false`). Schemas are created by `docker/postgres/init.sql`.

### Auth

- `POST /auth/register`, `/auth/login` → access JWT + rotating refresh token (stored hashed).
- Claims: `userId`, `roles`, `sub` (email). Access TTL from `JWT_EXPIRATION_SECONDS` (default 3600s).
- `GET /auth/me`, `POST /auth/refresh`.

### Catalog

- Movies: `ILIKE` search on title/director/genre, Spring Data pagination, `@Cacheable` on `GET /movies/{id}`.
- Theaters load rooms with `@EntityGraph` (avoids lazy-init with `open-in-view=false`).
- Showtimes call movie/theater over OpenFeign. Circuit breaker `showtime-service` (window 10, failure rate 50%, 5s open) + retry (3 × 1s). Health exposes `circuitbreakers`.

### Booking (hexagonal)

```
adapter/incoming/web|kafka
        │
        ▼
application/BookingApplicationService   ← BookingUseCase
        │
        ├── ShowtimePort        (Feign)
        ├── SeatLockPort        (Redis SET NX, 10 min TTL)
        ├── BookingPersistencePort
        └── OutboxPort
```

`domain/` has no Spring/JPA/Kafka types. Adapters live under `adapter/outgoing/{persistence,redis,feign}`.

---

## Seat consistency and payment saga

Double-booking is blocked at two layers; payment is decoupled from the HTTP request.

```
Client                 Booking                 Kafka                 Payment
  │                      │                       │                     │
  │ POST /bookings       │                       │                     │
  │ Idempotency-Key      │                       │                     │
  │─────────────────────►│ Redis SET NX seats    │                     │
  │                      │ INSERT booking PENDING│                     │
  │                      │ INSERT booking_seats  │                     │
  │                      │   UNIQUE(showtime,seat)                     │
  │                      │ INSERT outbox PENDING │                     │
  │ 201 PENDING          │                       │                     │
  │◄─────────────────────│                       │                     │
  │                      │ poll outbox 1s        │                     │
  │                      │──────────────────────►│ payment-requested   │
  │                      │                       │────────────────────►│ charge once / bookingId
  │                      │                       │◄────────────────────│ payment-completed
  │                      │ CONFIRMED | FAILED    │                     │
  │                      │ release seats on fail │                     │
```

**Holds.** `SET NX` per `(showtimeId, seat)` with 10 minute TTL. Failed persist releases the keys.

**Hard uniqueness.** `booking_seats (showtime_id, seat_number)` unique. `DataIntegrityViolationException` → `409` + lock release.

**Idempotency.** Unique `bookings.idempotency_key`. Replay of the same `Idempotency-Key` returns the original booking (no second hold).

**Outbox.** Booking row + seat rows + `outbox_events` share one transaction. `OutboxRelayService` (`@Scheduled` 1s) publishes `PENDING` rows to Kafka then marks `PUBLISHED`. That avoids “DB committed, producer failed”.

**Payment.** `PaymentService.processPayment` is idempotent on `bookingId`. `SimulatedCardGateway` is a strategy `PaymentGateway`. Success/failure is emitted on `payment-completed`; booking confirms or compensates (`FAILED` + delete seat rows + Redis release). Optimistic `@Version` on `bookings`.

**Conflicts.** RFC 7807 `application/problem+json`: `409` for held/booked seats, `404` missing aggregates, `400` validation.

---

## Observability

| Signal | Where |
|--------|--------|
| Metrics | `/actuator/prometheus` on every process; Prometheus scrape `host.docker.internal:{port}` |
| Traces | Micrometer Tracing + Brave, sample rate `1.0`, Zipkin |
| Logs | `%d [%X{correlationId}] [%X{traceId}]` |
| Dashboards | Grafana `Movie Booking Platform` (JVM CPU/heap, HTTP server requests) |
| Health | `show-details: always`; booking includes circuit breakers |

---

## Local run

Java 17, Docker. Compose publishes infra on the host and uses `extra_hosts: *:host-gateway` so services reach Postgres/Redis/Kafka/Eureka when container overlay DNS is unavailable.

```bash
cp .env.example .env
make package                 # ./mvnw -DskipTests package per module
docker compose up --build -d
# Eureka http://localhost:8761 should list all seven apps
make smoke
```

| UI | URL |
|----|-----|
| Eureka | http://localhost:8761 |
| Aggregated OpenAPI | http://localhost:8090/swagger-ui.html |
| Zipkin | http://localhost:9411 |
| Prometheus targets | http://localhost:9090/targets |
| Grafana | http://localhost:3000 (`admin` / `admin`) |

`k8s/base.yaml` is a sample namespace + ConfigMap + Eureka deployment, not a full prod chart.

### API sketch

```bash
TOKEN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@cinema.local","password":"User@123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl 'http://localhost:8090/api/v1/movies?q=Inception&size=5'
curl http://localhost:8090/api/v1/showtimes

curl -X POST http://localhost:8090/api/v1/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: demo-001" \
  -H 'Content-Type: application/json' \
  -d '{"showtimeId":1,"seats":["B1","B2"]}'

# poll until CONFIRMED
curl -H "Authorization: Bearer $TOKEN" http://localhost:8090/api/v1/bookings/me
curl -H "Authorization: Bearer $TOKEN" http://localhost:8090/api/v1/payments/1
```

Catalog mutations:

```bash
ADMIN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@cinema.local","password":"Admin@123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -X POST http://localhost:8090/api/v1/movies \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Oppenheimer","genre":"Drama","duration":180,"releaseDate":"2023-07-21","description":"Manhattan Project","director":"Christopher Nolan"}'
```

---

## Tests

```bash
make test
# one module
cd booking-service && ./mvnw test
```

H2 `test` profile: Flyway off, Eureka/Redis/Kafka disabled, in-memory `SeatLockPort`. Coverage is around auth tokens, movie cache/search, booking locks + idempotency + saga compensation, payment-once-per-bookingId.

CI (`.github/workflows/ci.yml`) runs `./mvnw test` per module.

---

## Layout

```
api-gateway/           WebFlux gateway, JWT, Redis rate limit, route + docs proxies
discovery-server/      Eureka
auth-service/          identity bounded context
movie-service/         catalog + Redis
theater-service/       theaters / rooms
showtime-service/      schedules, Feign, Resilience4j
booking-service/
  domain/              model, ports, domain exceptions
  application/         use-case + outbox relay
  adapter/             REST, Kafka, JPA, Redis, Feign
payment-service/       PaymentGateway strategy, Kafka
docker/postgres|prometheus|grafana
k8s/                   sample manifests
scripts/build-all.sh   package all modules
scripts/smoke-test.sh  login → catalog → book → payment
```

---

## Configuration

See `.env.example`.

| Variable | Default | Use |
|----------|---------|-----|
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | `cinema` | all JDBC URLs |
| `JWT_SECRET` | dev HS256 key | must be identical on gateway + resource servers |
| `REDIS_HOST` | `redis` | cache, locks, rate limit |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | booking + payment |
| `ZIPKIN_ENDPOINT` | `http://zipkin:9411/api/v2/spans` | traces |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka-server:8761/eureka/` | discovery |

Logical databases: `auth_db`, `movie_db`, `theater_db`, `showtime_db`, `booking_db`, `payment_db`.
