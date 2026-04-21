# Movie Booking Microservice System

[![CI Pipeline](https://github.com/YOUR_USERNAME/movie-booking-microservice/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/movie-booking-microservice/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

A production-ready movie ticket booking system built with a **microservices architecture** using Java 17, Spring Boot 3.4, Apache Kafka, and Docker. Demonstrates real-world backend engineering patterns including service discovery, async messaging, distributed tracing, circuit breaking, and API documentation.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Browser / API)                  │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP
                               ▼
              ┌────────────────────────────────┐
              │       API Gateway :8090         │
              │  (Spring Cloud Gateway)         │
              │  • Route /movies/**             │
              │  • Route /theaters/**           │
              │  • Route /showtimes/**          │
              │  • Route /bookings/**           │
              │  • Route /payments/**           │
              │  • Aggregated Swagger UI        │
              └──────────┬─────────────────────┘
                         │ Eureka Service Discovery
                         ▼
          ┌──────────────────────────────────────┐
          │      Eureka Discovery Server :8761    │
          └──────────────────────────────────────┘

   ┌──────────┐    ┌──────────┐    ┌──────────────────────┐
   │  Movie   │    │ Theater  │    │     Showtime          │
   │  :8091   │    │  :8092   │    │      :8093            │
   │  REST    │◄───┤  REST    │◄───┤  OpenFeign (CB) →     │
   └──────────┘    └──────────┘    │  movie / theater      │
                                   └──────────┬────────────┘
                                              │ Feign
                              ┌───────────────▼────────────┐
                              │     Booking Service :8094   │
                              │  OpenFeign (CB) → showtime  │
                              │  Kafka Producer →           │
                              │    "payment-topic"          │
                              └─────────────┬──────────────┘
                                            │ Kafka async
                              ┌─────────────▼──────────────┐
                              │    Payment Service :8095    │
                              │  Kafka Consumer             │
                              │  Kafka Producer →           │
                              │    "booking-update-topic"   │
                              └────────────────────────────┘

  Infrastructure:
    SQL Server :1433    Apache Kafka :9092    Zipkin :9411
```

---

## Key Features

| Feature | Implementation |
|---------|---------------|
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway with aggregated Swagger UI |
| **Async Messaging** | Apache Kafka (booking → payment flow) |
| **Inter-Service Calls** | OpenFeign with Resilience4j circuit breakers |
| **Distributed Tracing** | Micrometer Tracing + Zipkin |
| **API Documentation** | SpringDoc OpenAPI 3.0 (per-service Swagger + gateway aggregate) |
| **Error Handling** | RFC 7807 Problem Details responses |
| **Input Validation** | Bean Validation (`@Valid`, `@NotBlank`, `@Min`) |
| **Health Monitoring** | Spring Boot Actuator with circuit breaker health indicators |
| **CI/CD** | GitHub Actions (build, test, Docker image per service) |
| **Containerization** | Docker Compose with health checks and dependency ordering |

---

## Technology Stack

- **Runtime**: Java 17, Spring Boot 3.4.4
- **Microservices**: Spring Cloud 2024.0.1, Eureka, Spring Cloud Gateway, OpenFeign
- **Messaging**: Apache Kafka + Zookeeper (Confluent images)
- **Database**: Microsoft SQL Server 2019 (Spring Data JPA / Hibernate)
- **Resilience**: Resilience4j (circuit breakers, retry)
- **Observability**: Micrometer Tracing, Zipkin, Spring Actuator
- **API Docs**: SpringDoc OpenAPI 3.0 (Swagger UI)
- **Build**: Maven 3.x (Maven Wrapper per service)
- **Infrastructure**: Docker + Docker Compose

---

## Services

| Service | Port | Description |
|---------|------|-------------|
| `api-gateway` | 8090 | Entry point — routes all traffic, aggregated Swagger UI |
| `discovery-server` | 8761 | Eureka service registry |
| `movie-service` | 8091 | Movie CRUD (`/movies/**`) |
| `theater-service` | 8092 | Theater and room management (`/theaters/**`) |
| `showtime-service` | 8093 | Showtime scheduling with Feign + circuit breakers (`/showtimes/**`) |
| `booking-service` | 8094 | Seat booking, Kafka producer (`/bookings/**`) |
| `payment-service` | 8095 | Payment processing, Kafka consumer (`/payments/**`) |

---

## Async Booking Flow

```
1. POST /bookings        →  Booking created (status: PENDING)
                         →  Publishes JSON to "payment-topic"

2. payment-service       →  Consumes "payment-topic"
                         →  Processes payment
                         →  Publishes result to "booking-update-topic"

3. booking-service       →  Consumes "booking-update-topic"
                         →  Updates booking status (CONFIRMED / FAILED)
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker Desktop

---

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/movie-booking-microservice.git
cd movie-booking-microservice
```

### 2. Configure environment variables

```bash
cp .env.example .env
# Edit .env — set SA_PASSWORD and database credentials
```

### 3. Build all services

```bash
for service in discovery-server api-gateway movie-service theater-service showtime-service booking-service payment-service; do
  (cd $service && ./mvnw clean package -DskipTests)
done
```

### 4. Start the full stack

```bash
docker-compose up --build -d
```

### 5. Verify startup

```bash
# Eureka dashboard (wait ~30s for services to register)
open http://localhost:8761

# Aggregated Swagger UI (all 5 services)
open http://localhost:8090/swagger-ui.html

# Distributed tracing
open http://localhost:9411
```

---

## API Documentation

Each service exposes its own Swagger UI at `http://localhost:<port>/swagger-ui.html`.

The API Gateway aggregates all service docs at **http://localhost:8090/swagger-ui.html** — use the dropdown to switch between services.

### Example Requests

**Create a movie:**
```bash
curl -X POST http://localhost:8090/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "genre": "Sci-Fi",
    "duration": 148,
    "releaseDate": "2010-07-16",
    "description": "A mind-bending thriller",
    "director": "Christopher Nolan"
  }'
```

**Create a theater:**
```bash
curl -X POST http://localhost:8090/theaters \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cineplex Downtown",
    "location": "123 Main St",
    "contactInfo": "info@cineplex.com"
  }'
```

**Book seats:**
```bash
curl -X POST "http://localhost:8090/bookings?userId=1&showtimeId=1" \
  -H "Content-Type: application/json" \
  -d '["A1", "A2", "A3"]'
```

**Get user bookings:**
```bash
curl http://localhost:8090/bookings/user/1
```

**Check payment status:**
```bash
curl http://localhost:8090/payments/1
```

**Validation error example** (RFC 7807 Problem Details):
```bash
curl -X POST http://localhost:8090/movies \
  -H "Content-Type: application/json" \
  -d '{"genre": "Sci-Fi"}'
# Response: 400 with structured error + field-level messages
```

---

## Health & Observability

```bash
# Service health (includes circuit breaker state)
curl http://localhost:8094/actuator/health  # booking-service

# Gateway routes
curl http://localhost:8090/actuator/gateway/routes

# Metrics
curl http://localhost:8091/actuator/metrics

# Distributed traces
open http://localhost:9411
```

---

## Running Tests

```bash
# Run tests for a specific service
cd movie-service && ./mvnw test

# Run all service tests
for service in movie-service theater-service showtime-service booking-service payment-service; do
  echo "Testing $service..."
  (cd $service && ./mvnw test)
done
```

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `SA_PASSWORD` | SQL Server SA password |
| `SPRING_DATASOURCE_URL` | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `ZIPKIN_ENDPOINT` | Zipkin endpoint (default: `http://zipkin:9411/api/v2/spans`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka address (default: `localhost:9092`) |

---

## Project Structure

```
movie-booking-microservice/
├── .github/workflows/ci.yml       # GitHub Actions CI pipeline
├── docker-compose.yml             # Full stack with health checks
├── .env.example                   # Environment variable template
├── sql/
│   ├── create.sql                 # Database schema
│   └── insert.sql                 # Sample data
├── api-gateway/                   # Spring Cloud Gateway + Swagger aggregation
├── discovery-server/              # Eureka Service Registry
├── movie-service/                 # Movie CRUD + OpenAPI + validation + tests
├── theater-service/               # Theater management + OpenAPI
├── showtime-service/              # Scheduling + Feign + Resilience4j
├── booking-service/               # Booking flow + Kafka + circuit breaker + tests
└── payment-service/               # Payment processing + Kafka consumer
```
