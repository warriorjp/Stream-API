# Microservices Design Patterns

## ✅ API Gateway

An **API Gateway** is an essential component in a microservices
architecture, acting as a **single entry point** for all client
requests.

It provides functionalities such as: - Routing - Authentication &
Authorization - Load Balancing - Rate Limiting - Request/Response
Transformation - Monitoring & Logging

### Benefits

-   Single entry point for clients
-   Simplifies client communication
-   Improves security
-   Centralized cross-cutting concerns
-   Better monitoring and analytics

------------------------------------------------------------------------

## ✅ Service Registry

The **Service Registry** enables services to **discover and
communicate** with each other dynamically.

It maintains a registry of: - Service Name - IP Address - Port -
Metadata - Health Status

### Benefits

-   Dynamic service discovery
-   Load balancing support
-   No hardcoded service URLs
-   High availability

**Examples:** Eureka, Consul, Kubernetes Service Discovery

------------------------------------------------------------------------

## ✅ Circuit Breaker

The **Circuit Breaker** pattern prevents cascading failures when a
downstream service becomes slow or unavailable.

### Why Use a Circuit Breaker?

-   Stops repeated calls to failing services.
-   Invokes a fallback method when retries fail.
-   Prevents thread and resource exhaustion.
-   Improves overall system resilience.
-   Allows the failed service time to recover.

### Circuit Breaker States

#### 1. Closed

-   Requests flow normally.
-   Failures are monitored.
-   Opens after reaching the failure threshold.

#### 2. Open

-   Requests fail immediately (Fail Fast).
-   Fallback method is executed.
-   No calls are sent to the downstream service.

#### 3. Half-Open

-   Limited requests are allowed.
-   If successful → Circuit closes.
-   If failed → Circuit opens again.

### Common Implementations

-   Resilience4j
-   Spring Cloud Circuit Breaker
-   Hystrix (Deprecated)

------------------------------------------------------------------------

## ✅ Database per Service

Each microservice owns its own database.

### Benefits

-   **Data Isolation** -- Prevents tight coupling.
-   **Independent Scaling** -- Scale databases independently.
-   **Technology Flexibility** -- Different databases per service.
-   **Security** -- Restricted data ownership.
-   **Failure Isolation** -- One database failure doesn't impact others.

------------------------------------------------------------------------

## ✅ Saga Pattern

The **Saga Pattern** manages distributed transactions across multiple
microservices without using **Two-Phase Commit (2PC)**.

### Types of Saga

#### 1. Orchestration

A central **Saga Orchestrator** controls the workflow.

**Advantages** - Easier monitoring - Centralized error handling - Better
control

#### 2. Choreography

Each service communicates through events.

**Advantages** - No central controller - Loosely coupled services -
Highly scalable

**Example Message Broker** - Kafka

------------------------------------------------------------------------

## ✅ CQRS (Command Query Responsibility Segregation)

CQRS separates **Write (Command)** operations from **Read (Query)**
operations.

### Benefits

  -----------------------------------------------------------------------
  Benefit                       Explanation
  ----------------------------- -----------------------------------------
  Scalability                   Scale read and write independently

  Performance                   Optimize reads and writes separately

  Separation of Concerns        Business logic and query logic remain
                                independent

  Flexibility                   SQL for writes and NoSQL for reads
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# Interview Summary

  Pattern                Purpose
  ---------------------- ------------------------------------
  API Gateway            Single entry point for clients
  Service Registry       Dynamic service discovery
  Circuit Breaker        Prevent cascading failures
  Database per Service   Independent data ownership
  Saga Pattern           Distributed transaction management
  CQRS                   Separate read and write models
