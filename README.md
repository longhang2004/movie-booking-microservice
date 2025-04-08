# Movie Booking System

A microservices-based application for booking movie tickets, managing theaters, showtimes, and payments. Built with Spring Boot, Spring Cloud, Kafka, and SQL Server.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Environment Variables](#environment-variables)
- [Contributing](#contributing)

## Overview

The Movie Booking System is a scalable microservices application designed to allow users to:
- Browse movies, theaters, and showtimes.
- Book movie tickets.
- Process payments securely.

It leverages Spring Cloud Gateway for request routing, Eureka for service discovery, Kafka for asynchronous communication, and SQL Server for persistent storage.

## Architecture

The system is built using a microservices architecture with the following components:

- **API Gateway**: Routes incoming client requests to appropriate microservices.
- **Discovery Server (Eureka)**: Registers and discovers microservices dynamically.
- **Microservices**: Handle specific business logic (movies, theaters, showtimes, bookings, payments).
- **Kafka**: Facilitates asynchronous messaging between `booking-service` and `payment-service`.
- **SQL Server**: Stores data for all services, configurable for Azure SQL.

## Microservices

| Service            | Port  | Description                              |
|--------------------|-------|------------------------------------------|
| `movie-service`    | 8091  | Manages movie information.              |
| `theater-service`  | 8092  | Manages theater and room details.       |
| `showtime-service` | 8093  | Manages movie showtimes.                |
| `booking-service`  | 8094  | Handles ticket bookings and Kafka messaging. |
| `payment-service`  | 8095  | Processes payments and updates bookings via Kafka. |
| `api-gateway`      | 8090  | Entry point for all client requests.    |
| `discovery-server` | 8761  | Eureka-based service registry.          |

## Technologies

- **Java**: 17
- **Spring Boot**: 3.1.0
- **Spring Cloud**: 2022.0.4
- **Kafka**: Asynchronous messaging
- **SQL Server**: Database (configurable for Azure SQL)
- **Docker & Docker Compose**: Containerization and orchestration

## Prerequisites

Before running the project, ensure you have the following installed:
- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) (or OpenJDK)
- [Maven](https://maven.apache.org/download.cgi) for building the project
- [Docker](https://www.docker.com/products/docker-desktop) for containerization
- [Git](https://git-scm.com/downloads) for cloning the repository

## Setup

### Clone the Repository

```bash
git clone https://github.com/your-username/movie-booking-system.git
cd movie-booking-system
```
## Configure Environment Variables

Sensitive information (e.g., Azure SQL credentials) is not included in the repository. Create a ```.env``` file in the project root with the following content:

```bash
SPRING_DATASOURCE_URL=jdbc:sqlserver://your-azure-sql-server.database.windows.net:1433;databaseName=yourdb;encrypt=true;trustServerCertificate=true
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password
```

Replace your-azure-sql-server, yourdb, your-username, and your-password with your actual Azure SQL Server details. Of course, you can use your local database, or another cloud database if you want to, as long as you fill in all the fields in the ```.env``` file.

### Build the project

Build all microservices using Maven to generate JAR files:

```bash 
mvn clean package -pl api-gateway,movie-service,theater-service,showtime-service,booking-service,payment-service,discovery-server -am
```

This creates JAR files in the target/ directory of each service.

## Running the Application

### Using Docker Compose

1. **Build Docker images**:
   ```bash
   docker-compose build
   ```

2. **Start all services**:
   ```bash
   docker-compose up
   ```

   To run in detached mode:
   ```bash
   docker-compose up -d
   ```

3. **Stop the application**:
   ```bash
   docker-compose down
   ```

   To remove volumes (e.g., SQL Server data):
   ```bash
   docker-compose down -v
   ```

### Verify Services

- **Eureka Dashboard**: Open `http://localhost:8761/` in your browser to check registered services.
- **API Gateway**: Test endpoints via `http://localhost:8090/`.
- **View logs**:
   ```bash
   docker logs <service-name>
   ```

## API Endpoints

### Movie Service
- **GET /movies**: Retrieve all movies.
- **POST /movies**: Add a new movie.
  ```bash
  curl -X POST http://localhost:8090/movies -H "Content-Type: application/json" -d '{"title":"Inception","genre":"Sci-Fi","duration":148,"releaseDate":"2010-07-16","description":"A thief...","director":"Christopher Nolan"}'
  ```

### Theater Service
- **GET /theaters**: Retrieve all theaters.
- **POST /theaters**: Add a new theater.
  ```bash
  curl -X POST http://localhost:8090/theaters -H "Content-Type: application/json" -d '{"name":"CGV Vincom","location":"HCMC","contactInfo":"0123-456-789","rooms":[{"name":"Room 1","capacity":100}]}'
  ```

### Showtime Service
- **GET /showtimes**: Retrieve all showtimes.
- **POST /showtimes**: Add a new showtime.
  ```bash
  curl -X POST http://localhost:8090/showtimes -H "Content-Type: application/json" -d '{"movieId":1,"theaterId":1,"roomId":1,"startTime":"2025-04-10T18:00:00","endTime":"2025-04-10T20:30:00","price":150000.0}'
  ```

### Booking Service
- **GET /bookings/{userId}**: Retrieve bookings by user ID.
- **POST /bookings**: Create a new booking.
  ```bash
  curl -X POST "http://localhost:8090/bookings?userId=1&showtimeId=1" -H "Content-Type: application/json" -d '["A1","A2"]'
  ```

### Payment Service
- **GET /payments/{bookingId}**: Retrieve payment by booking ID.
- **POST /payments**: Process a payment.
  ```bash
  curl -X POST "http://localhost:8090/payments?bookingId=1&userId=1&amount=300000.0"
  ```

## Environment Variables

The following environment variables are required (set in `.env` or your environment):

| Variable                  | Description                          | Example Value                                      |
|---------------------------|--------------------------------------|---------------------------------------------------|
| `SPRING_DATASOURCE_URL`   | Database connection string          | `jdbc:sqlserver://your-server...`                 |
| `SPRING_DATASOURCE_USERNAME` | Database username                | `your-username`                                   |
| `SPRING_DATASOURCE_PASSWORD` | Database password                | `your-password`                                   |

## Contributing

We welcome contributions! Follow these steps:
1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`.
3. Commit your changes: `git commit -m "Add your feature"`.
4. Push to the branch: `git push origin feature/your-feature`.
5. Open a Pull Request.

