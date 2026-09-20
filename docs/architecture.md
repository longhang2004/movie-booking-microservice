# Architecture

This document describes the **local lab** topology. One replica of each process, demo credentials, simulated payments. See the [README disclaimer](../README.md#what-this-is-not).

## Container view

```mermaid
flowchart LR
  subgraph host [Host machine]
    C["curl / browser / k6"]
  end

  subgraph compose [docker compose]
    GW["api-gateway :8090"]
    EU["eureka :8761"]
    A["auth :8096"]
    M["movie :8091"]
    T["theater :8092"]
    S["showtime :8093"]
    B["booking :8094"]
    P["payment :8095"]
    PG[("postgres :5432")]
    R[("redis :6379")]
    K["kafka :9092"]
    Z["zipkin :9411"]
    PR["prometheus :9090"]
    G["grafana :3000"]
  end

  C --> GW
  GW --> EU
  GW --> A & M & T & S & B & P
  S -->|Feign| M
  S -->|Feign| T
  B -->|Feign| S
  B <-->|outbox / saga| K
  P <--> K
  A & M & T & S & B & P --> PG
  M & B & GW --> R
  GW & A & B -.-> Z
  PR --> G
```

Compose binds infra ports on localhost and sets `extra_hosts: *:host-gateway` so containers can reach those ports when overlay DNS is unavailable. That is a **lab workaround**, not how services should discover each other in a real cluster.

## Why each box exists

| Box | Role in the lab |
|-----|-----------------|
| api-gateway | Single HTTP entry, JWT, Redis `INCR` rate limit, `X-Correlation-Id`, `lb://` routes, Swagger aggregation |
| discovery-server | Eureka registry so `lb://movie-service` resolves |
| auth-service | Register/login/refresh, RS256 access JWT, hashed refresh, lockout, JWKS |
| movie / theater / showtime | Catalog. Showtimes call the other two over Feign + Resilience4j |
| booking-service | Hexagonal seat hold + outbox |
| payment-service | Kafka consumer, `SimulatedCardGateway`, idempotent on `bookingId` |
| platform-security | Shared jar: decoder, PEM, correlation filter, RFC 7807 |

## JWT and JWKS

```mermaid
flowchart LR
  subgraph auth [auth-service]
    PEM["docker/jwt/*.pem<br/>kid = auth-service-rsa"]
    ENC["JwtEncoder RS256"]
    JWKS["GET /auth/.well-known/jwks.json"]
    PEM --> ENC
    PEM --> JWKS
  end

  subgraph others [gateway + resource servers]
    DEC["NimbusJwtDecoder<br/>iss + aud validators"]
  end

  ENC -->|"access JWT"| Client
  Client -->|"Authorization: Bearer"| others
  JWKS --> DEC
  DEC --> others
```

- Access token claims: `iss=movie-booking`, `aud=movie-booking-api`, `sub` (email), `userId`, `roles`.
- Resource servers prefer JWKS; tests may inject `app.jwt.public-key` so they do not call auth.
- Empty PEM → ephemeral 2048-bit pair (unit tests). Compose mounts the lab files so `kid` stays stable across auth restarts.
- Refresh tokens are opaque, stored as SHA-256. Rotation revokes the previous row; a revoked token revokes the **family**.

Catalog GET is anonymous. Catalog writes require `ROLE_ADMIN`. Booking and payment require an authenticated user; booking reads are owner-or-admin.

## Hexagonal booking

```mermaid
flowchart TB
  WEB["adapter/incoming/web<br/>BookingController"]
  KAFKA_IN["adapter/incoming/kafka<br/>PaymentCompletedListener"]
  APP["application/BookingApplicationService"]
  DOM["domain/<br/>Booking, ports, exceptions"]
  FEIGN["adapter/outgoing/feign"]
  REDIS["adapter/outgoing/redis<br/>SET NX 10 min"]
  JPA["adapter/outgoing/persistence<br/>booking + seats + outbox"]
  RELAY["application/OutboxRelayService<br/>@Scheduled 1s"]

  WEB --> APP
  KAFKA_IN --> APP
  APP --> DOM
  APP --> FEIGN
  APP --> REDIS
  APP --> JPA
  RELAY --> JPA
```

`domain/` has no Spring, JPA, or Kafka types. Tests swap Redis for an in-memory `SeatLockPort`.

## Rate limit (gateway)

| Path | Limit | Redis key |
|------|-------|-----------|
| `POST .../auth/login` | 10 / min / IP | `rl:login:{ip}` |
| Other proxied HTTP | 60 / min / IP | `rl:{ip}` |
| Health, Prometheus, OpenAPI, JWKS | excluded | — |

Over limit → `429`. k6 from one machine is one IP; keep browse at ~1 rps or expect 429. See [testing.md](testing.md#k6-lab).

## Observability

```mermaid
flowchart LR
  SVC["Each Spring process"]
  SVC -->|"X-Correlation-Id + MDC"| LOGS["stdout logs"]
  SVC -->|"Brave, sample 1.0"| Z["Zipkin"]
  SVC -->|"/actuator/prometheus"| P["Prometheus"]
  P --> G["Grafana dashboard"]
```

Health: `/actuator/health` (show-details never on the public actuator in services). Booking health includes Resilience4j circuit breakers.

## Kubernetes sample

`k8s/base.yaml` is a **namespace + ConfigMap + Eureka Deployment** to show wiring, not a production chart (no HPA, PDB, TLS, or secrets operator).
