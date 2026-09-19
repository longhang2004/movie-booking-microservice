# Movie Booking Platform

[![CI Pipeline](https://github.com/longhang2004/movie-booking-microservice/actions/workflows/ci.yml/badge.svg)](https://github.com/longhang2004/movie-booking-microservice/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Kafka-KRaft-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

A portfolio-grade movie ticket booking platform built to match what **Middle Java Backend** roles typically ask for: Spring Boot 3, JWT/OAuth2, Redis, PostgreSQL, Kafka, hexagonal architecture, transactional outbox, observability, Docker, and CI.

This is intentionally over-engineered as a personal learning project.

---

## Why this architecture

Hiring JDs for Middle Java (Zalopay, logistics platforms, product companies, banks) consistently list:

- Java 17 + Spring Boot, REST, JPA, SOLID
- JWT / Spring Security, Redis caching
- PostgreSQL, Flyway, unit + integration tests
- Microservices, Kafka / event-driven flows
- Docker, CI/CD, monitoring, resilience (circuit breaker, retry)
- Clean / hexagonal architecture on the core domain

This repo implements those pieces in a single runnable system.

---

## Architecture

```
                    Browser / curl
                           │
                           ▼
              ┌─────────────────────────┐
              │  API Gateway :8090      │
              │  JWT resource server    │
              │  Redis rate limit       │
              │  CORS + correlation id  │
              │  Aggregated Swagger     │
              └────────────┬────────────┘
                           │ Eureka
              ┌────────────▼────────────┐
              │  Discovery :8761        │
              └─────────────────────────┘

  auth :8096     movie :8091     theater :8092     showtime :8093
  JWT issue      Redis cache     rooms              Feign + CB
  refresh tokens paginated search                   ──► movie/theater

              booking :8094  (hexagonal)
              Redis seat locks
              DB unique (showtime, seat)
              Transactional outbox ──► Kafka payment-requested
                           │
                           ▼
              payment :8095
              Strategy gateway + idempotent consumer
              Kafka payment-completed ──► booking saga (confirm / release seats)

  postgres (db-per-service)   redis   kafka (KRaft)   zipkin   prometheus   grafana
```

### Booking saga

1. `POST /api/v1/bookings` with JWT + optional `Idempotency-Key`
2. Redis `SET NX` holds seats (10 min TTL)
3. Same DB transaction: insert booking `PENDING` + unique seat rows + outbox row
4. Outbox relay publishes `payment-requested`
5. Payment service charges (simulated card gateway) **once per bookingId**
6. `payment-completed` confirms the booking, or fails and releases seats

---

## Tech stack

| Area | Choice |
|------|--------|
| Runtime | Java 17, Spring Boot 3.4.4, Spring Cloud 2024.0.1 |
| Identity | Spring Security + OAuth2 Resource Server, JWT HS256, refresh tokens, RBAC (`USER` / `ADMIN`) |
| Data | PostgreSQL 16, **database per service**, Flyway, optimistic locking |
| Cache / locks | Redis 7 (movie cache, seat holds, gateway rate limit) |
| Messaging | Kafka KRaft, transactional outbox, idempotent payment consumer |
| Resilience | OpenFeign + Resilience4j circuit breaker + retry + fallback |
| Observability | Actuator, Micrometer Prometheus, Zipkin, Grafana, correlation IDs |
| API | SpringDoc OpenAPI 3, RFC 7807 Problem Details, pagination |
| Delivery | Docker Compose, sample Kubernetes manifests, GitHub Actions |

---

## Services

| Service | Port | Notes |
|---------|------|--------|
| `api-gateway` | 8090 | JWT, rate limiting, `/api/v1/**` facade |
| `discovery-server` | 8761 | Eureka |
| `auth-service` | 8096 | Register / login / refresh / me |
| `movie-service` | 8091 | Catalog search + Redis cache. Writes require `ADMIN` |
| `theater-service` | 8092 | Theaters and rooms |
| `showtime-service` | 8093 | Schedules, Feign to movie/theater |
| `booking-service` | 8094 | Hexagonal core: ports, Redis locks, outbox |
| `payment-service` | 8095 | Strategy `PaymentGateway`, Kafka consumer |

---

## Quick start

```bash
cp .env.example .env
make package          # ./mvnw package in every module
docker compose up --build -d
# wait until Eureka shows all instances (http://localhost:8761)
make smoke
```

### Demo accounts (seeded)

| Email | Password | Role |
|-------|----------|------|
| `admin@cinema.local` | `Admin@123` | ADMIN |
| `user@cinema.local` | `User@123` | USER |

### Useful UIs

- Eureka: http://localhost:8761
- Swagger (gateway): http://localhost:8090/swagger-ui.html
- Zipkin: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (`admin` / `admin`)

---

## Example API flow

```bash
# login
TOKEN=$(curl -s -X POST http://localhost:8090/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@cinema.local","password":"User@123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

# public catalog
curl 'http://localhost:8090/api/v1/movies?q=Inception&size=5'
curl http://localhost:8090/api/v1/showtimes

# book two seats (async payment)
curl -X POST http://localhost:8090/api/v1/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: demo-001" \
  -H 'Content-Type: application/json' \
  -d '{"showtimeId":1,"seats":["B1","B2"]}'

curl -H "Authorization: Bearer $TOKEN" http://localhost:8090/api/v1/bookings/me
```

Admin-only catalog writes:

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
# or one module
cd booking-service && ./mvnw test
```

Unit tests cover auth, movie catalog, booking use-cases (locks, idempotency, saga compensation) and payment idempotency. Each service uses an H2 `test` profile (Flyway/Redis/Kafka/Eureka disabled).

---

## Project layout

```
auth-service/          JWT identity bounded context
booking-service/
  domain/              entities, ports
  application/         use-cases + outbox relay
  adapter/             REST, Kafka, JPA, Redis, Feign
movie-service/         cached catalog
theater-service/
showtime-service/      Feign + circuit breaker
payment-service/       payment gateway strategy
api-gateway/
discovery-server/
docker/                postgres init, prometheus, grafana
k8s/                   sample Kubernetes manifests
scripts/               build-all + smoke test
```

---

## Environment

See `.env.example`. Database-per-service names: `auth_db`, `movie_db`, `theater_db`, `showtime_db`, `booking_db`, `payment_db`.
