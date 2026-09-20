# Flows

Lab behavior only. Commands assume Compose is up on [http://localhost:8090](http://localhost:8090).

## HTTP through the gateway

```mermaid
sequenceDiagram
  actor Client
  participant GW as api-gateway
  participant Redis
  participant EU as Eureka
  participant Svc as downstream service

  Client->>GW: HTTP /api/v1/...
  GW->>GW: X-Correlation-Id (create or echo)
  alt login
    GW->>Redis: INCR rl:login:{ip} (10/min)
  else other
    GW->>Redis: INCR rl:{ip} (60/min)
  end
  alt over limit
    GW-->>Client: 429
  else JWT required and missing/invalid
    GW-->>Client: 401
  else ok
    GW->>EU: resolve lb://service
    GW->>Svc: rewritten path + Bearer + correlation id
    Svc-->>GW: response
    GW-->>Client: response
  end
```

Anonymous:

- `POST /api/v1/auth/register|login|refresh`
- `GET /auth/.well-known/jwks.json`
- `GET` movies, theaters, showtimes
- `/actuator/health`, `/actuator/prometheus`, Swagger

Everything else needs a valid RS256 JWT (`iss` / `aud` match).

## Login and access token

```mermaid
sequenceDiagram
  actor User
  participant GW as api-gateway
  participant AUTH as auth-service
  participant DB as auth_db

  User->>GW: POST /api/v1/auth/login
  GW->>AUTH: POST /auth/login
  AUTH->>DB: load user by email
  alt bad password
    AUTH->>DB: failedLoginAttempts++
    alt 5 failures
      AUTH->>DB: lock 15 min
      AUTH-->>User: 423 Locked
    else
      AUTH-->>User: 401
    end
  else locked
    AUTH-->>User: 423
  else ok
    AUTH->>AUTH: RS256 access JWT
    AUTH->>DB: insert SHA-256 refresh hash
    AUTH-->>User: accessToken + refreshToken
  end
```

Refresh: new access JWT + new opaque refresh; previous hash revoked. Presenting a **revoked** refresh revokes the whole family (reuse detection).

## Seat hold and payment saga

```mermaid
sequenceDiagram
  autonumber
  actor Client
  participant BK as booking-service
  participant ST as showtime-service
  participant Redis
  participant DB as booking_db
  participant Kafka
  participant PAY as payment-service

  Client->>BK: POST /bookings<br/>Idempotency-Key, seats
  BK->>DB: lookup idempotency_key
  alt key already used
    BK-->>Client: 200/201 original booking
  else new
    BK->>ST: Feign GET showtime
    loop each seat
      BK->>Redis: SET NX booking:seat:{showtime}:{seat} 10m
    end
    alt NX failed
      BK->>Redis: DEL acquired keys
      BK-->>Client: 409 problem+json
    else
      BK->>DB: BEGIN<br/>booking PENDING<br/>booking_seats UNIQUE<br/>outbox PENDING
      alt unique violation
        BK->>Redis: DEL
        BK-->>Client: 409
      else
        BK-->>Client: 201 PENDING
      end
    end
  end

  loop OutboxRelay 1s
    BK->>Kafka: payment-requested
    BK->>DB: outbox PUBLISHED
  end

  Kafka->>PAY: payment-requested
  PAY->>PAY: SimulatedCardGateway<br/>idempotent on bookingId
  PAY->>Kafka: payment-completed SUCCESS/FAILED
  Kafka->>BK: payment-completed
  alt SUCCESS
    BK->>DB: status CONFIRMED @Version
  else FAILED
    BK->>DB: FAILED, delete seat rows
    BK->>Redis: DEL holds
  end
```

Client is **not** blocked on payment. Poll `GET /api/v1/bookings/{id}` until `CONFIRMED` or `FAILED` (smoke waits ~4s; k6 polls up to ~10s).

## Conflict responses

| Situation | Status | Type suffix |
|-----------|--------|-------------|
| Seat held in Redis or unique constraint | 409 | `conflict` |
| Missing movie/theater/booking/payment | 404 | `not-found` |
| Bean validation | 400 | `validation` |
| Account locked | 423 | `locked` |
| Bad credentials / refresh reuse | 401 | `unauthorized` |
| Gateway rate limit | 429 | — |

Bodies are RFC 7807 `application/problem+json` (`timestamp`, `path`). Shared builder: `platform-security` `ProblemDetails`.

## Catalog write (admin)

```mermaid
sequenceDiagram
  actor Admin
  participant GW as api-gateway
  participant MOV as movie-service

  Admin->>GW: POST /api/v1/auth/login admin@cinema.local
  GW-->>Admin: JWT roles=ADMIN
  Admin->>GW: POST /api/v1/movies Bearer
  GW->>MOV: POST /movies
  MOV->>MOV: hasRole ADMIN
  MOV-->>Admin: 201
```

## k6 scenarios

`scripts/load/booking.js` (see [testing.md](testing.md)):

```mermaid
flowchart TB
  setup["setup: login once"]
  setup --> browse["browse: GET movies/theaters/showtimes<br/>1 rps, 200 or 429"]
  setup --> contend["contend: 12 VUs same showtime+seat<br/>one 201, rest 409"]
  setup --> saga["saga: 4 VUs unique seats<br/>poll until CONFIRMED/FAILED"]
```

Do **not** login per VU (login limiter is 10/min). Unique `Idempotency-Key` on every booking.
