# Testcontainers Demo with Spring Boot

This project demonstrates how to use Testcontainers for integration testing a Spring Boot application with multiple external dependencies:

* PostgreSQL for data storage
* Redis for caching
* Kafka for messaging

+-------------------------+
| Spring Boot Application |
|                         |
| +---------------------+ |
| |   Controllers       | |
| +---------------------+ |
| |   Services          | |
| +---------------------+ |
| |   Repositories      | |
| +---------------------+ |
+-----------|-------------+
|
v
+-----------|-------------+
|       PostgreSQL        |
|       (Database)        |
+-------------------------+
|
v
+-----------|-------------+
|          Redis          |
|       (Cache)           |
+-------------------------+
|
v
+-----------|-------------+
|          Kafka          |
|    (Message Broker)     |
+-------------------------+
|
v
+-----------|-------------+
|     Test Containers     |
| (Integration Testing)   |
+-------------------------+
## Features

* Spring Boot application with multiple dependencies
* Implementation of tests for PostgreSQL, Redis, and Kafka using Testcontainers
* Web UI to interact with the application
* Docker Compose setup for development environment

## Prerequisites

* Java 21+
* Maven
* Docker and Docker Compose
* Docker daemon must be running for tests that use Testcontainers

## Running the Application

### With Docker Compose (Recommended for Development)

1. Start the infrastructure services:

```bash
docker compose up -d
```

2. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

3. Access the web UI at http://localhost:8080

### Without Docker Compose

If you don't use Docker Compose, you'll need to:

1. Set up PostgreSQL, Redis, and Kafka services manually
2. Update the configuration in `application.properties` accordingly
3. Run the application:

```bash
mvn spring-boot:run
```

## Running the Tests

The tests use Testcontainers to spin up the required infrastructure automatically:

```bash
mvn test
```

Note: You don't need to have the infrastructure services running to execute the tests since Testcontainers will manage them.

## Project Structure

* `src/main/java/com/example/testcontainers/model`: Entity classes
* `src