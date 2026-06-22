# Zipcode Search API

> A resilient, observable Brazilian zipcode (CEP) lookup and street search API — built as a proving ground for the technologies behind a payment gateway.

<p>
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-17-blue">
  <img alt="Resilience4j" src="https://img.shields.io/badge/Resilience4j-2.4.0-yellow">
  <img alt="Observability" src="https://img.shields.io/badge/Prometheus%20%2B%20Grafana%20%2B%20Loki-orange">
  <img alt="Build" src="https://img.shields.io/badge/build-Maven-red">
</p>

---

## Overview

**Zipcode Search API** resolves Brazilian zip codes (CEP) and searches addresses by street name. It consumes external CEP providers, caches results in PostgreSQL, and exposes a clean REST API.

This project is a **proving ground**: a deliberate test to validate that I can apply the core technologies a payment gateway relies on — circuit breaking, resilient external integration, and full-stack observability — before building a payment gateway of my own from scratch. The CEP domain is just the vehicle; the real subject is the engineering around it. Many of the decisions below are deliberately scoped: the goal is to demonstrate *knowing when to apply* a pattern as much as *knowing how*.

---

## Key Features

- **Multi-source CEP resolution** — a provider abstraction lets the system fall back across multiple external sources without the business layer knowing which one answered.
- **Resilience** — retry with exponential backoff and a circuit breaker (Resilience4j), each independently tested.
- **Smart caching (deduplication)** — once a CEP is fetched, it is persisted; subsequent lookups hit the database, not the external API.
- **Fuzzy street search** — PostgreSQL `pg_trgm` similarity search with pagination.
- **Full-stack observability** — metrics (Prometheus), dashboards (Grafana), and centralized logs (Loki), all wired through Spring Boot Actuator and a structured logging policy.
- **Virtual threads (Java 21 / Loom)** — high-concurrency I/O to external providers without reactive programming.
- **OpenAPI / Swagger** — fully documented endpoints with request/response examples.

---

## Architecture

The codebase follows a **package-by-feature** layout. Address resolution is decoupled from any specific provider through a small abstraction:

```
Controller  ->  Service  ->  CepResolver  ->  CepProvider (ViaCep, ...)
                  |
                  +-------->  Repository (PostgreSQL cache)
```

- **`CepProvider`** — interface every external source implements (`fetch(cep)` returning a neutral `CepData` DTO).
- **`CepResolver`** — iterates providers in order, falling back to the next when one fails, and surfaces a domain-level result.
- **`ZipcodeService`** — owns caching and persistence only; it talks to the resolver, never to a concrete provider.

This means adding a new source (e.g. BrasilAPI) is a matter of implementing `CepProvider` — the service and resolver remain untouched. *(Open/closed in practice: extend without modifying.)*

### Validation & normalization

- **Validation happens at the border** (controller): zip-code format and street length are checked before any work is done.
- **Normalization happens internally**, so the rest of the system works with clean, consistent input.

### Data model note

CEP data is intentionally **denormalized** into a single table. Because this is a read-heavy lookup of stable reference data, normalizing `state`/`region`/etc. into separate tables would add joins and complexity with no real benefit at this scale.

### Deployment intent (production)

The application is designed to be **container-first and Kubernetes-friendly**: it is stateless, ships logs and metrics for scraping, and externalizes all configuration — so running it as one or more replicas in a cluster is the intended production shape.

One deliberate architectural stance: **in production, PostgreSQL should run *outside* the Kubernetes cluster** — as a managed service or a dedicated host — not as a pod. Databases are stateful and have very different lifecycle, storage, and failover needs than stateless app workloads; keeping the database off the cluster avoids a class of operational problems that running stateful sets in K8s tends to invite. The cluster runs the stateless app; the data lives on stable, dedicated infrastructure.

---

## Resilience

External provider calls are wrapped with **retry** and a **circuit breaker** (Resilience4j):

- **Retry** — up to 3 attempts with exponential backoff (500ms -> 1s). Retries only on technical failures (`ProviderException`); never on "not found" (`ZipcodeNotFound`), so a non-existent CEP fails fast.
- **Circuit breaker** — opens after a failure-rate threshold over a sliding window, then fails fast (`CallNotPermittedException`) instead of hammering a dead source, and probes again after a wait period.

Both behaviors are covered by dedicated, isolated tests (retry attempt counts; circuit state transitions).

The resolver's fallback and the per-provider resilience compose cleanly: each provider retries on its own; if it ultimately fails, the resolver moves to the next source.

---

## Observability

Full observability is in place — **metrics and logs are both collected and visualized in Grafana**, giving the classic "a metric tells you *something happened*, a log tells you *why*" workflow.

The stack has three pieces, all running via Docker Compose:

- **Prometheus** — scrapes application metrics from `/actuator/prometheus` (HTTP request latency and counts, JVM, HikariCP pool, and Resilience4j circuit-breaker / retry state).
- **Grafana** — dashboards over the Prometheus data (and Loki, below).
- **Loki** — centralized logs, shipped directly from the application via a Logback appender and queryable by label (e.g. `level="ERROR"`).

### Metrics dashboard (Grafana + Prometheus)

![Grafana metrics dashboard](https://i.imgur.com/zbNlJjV.png)

### Centralized logs (Grafana + Loki)

![Loki logs in Grafana](https://i.imgur.com/qFn5x3E.png)

### Structured logging policy

Log level reflects the *nature* of the event, not the layer:

- `ERROR` — unexpected failures (500) and "all providers failed".
- `WARN` — client-side errors (400/422/405) worth watching (bot / misconfigured integration), and a provider falling back.
- `INFO` — successful inserts (selected fields only, never the full payload).
- `DEBUG` — cache-hit vs. api-hit resolution, not-found, DB queries.

**Principle:** the log records the *event* (key + context); the database holds the *data*. Logs point to data, they don't duplicate it.

---

## Engineering Decisions & Scaling Path

A few capabilities were **deliberately not implemented** at this stage. They are documented here as conscious trade-offs and natural next steps under higher load — not as gaps:

| Capability | Why not now | When it would make sense |
|---|---|---|
| **Redis cache** | DB-level deduplication already caches results; read volume is low. | If PostgreSQL reads became a bottleneck under high traffic. |
| **Rate limiting** | This belongs **outside the application** — at the infrastructure edge (WAF / API gateway), not in the app code. | The app should never own rate limiting; it's an edge concern by design. |
| **Bulkhead / TimeLimiter** | Retry + circuit breaker already cover resilience at this volume. | Isolating thread pools under high concurrency across many sources. |

This section exists to show the reasoning about boundaries — when a pattern earns its complexity and when it doesn't.

---

## Tech Stack

**Application**
- **Java 21** (virtual threads enabled)
- **Spring Boot 4.1** (Web MVC, Data JPA, Validation, Actuator)
- **PostgreSQL 17** with `pg_trgm` for fuzzy search
- **Flyway** for migrations (`ddl-auto: validate`)
- **MapStruct** for DTO/entity mapping
- **Lombok**
- **Resilience4j** (retry + circuit breaker)
- **springdoc-openapi** (Swagger UI)

**Observability**
- **Micrometer + Prometheus** — metrics collection and scraping
- **Grafana** — dashboards
- **Loki** (loki-logback-appender) — centralized logging

**Testing**
- **JUnit 5 + Mockito + AssertJ**

---

## Observability Stack (Docker Compose)

The observability stack — **PostgreSQL, Prometheus, Grafana, and Loki** — runs as a single Docker Compose setup, so the whole environment comes up with one command.

```bash
docker compose up -d
```

| Service | Port | Purpose |
|---|---|---|
| PostgreSQL | `5433` | Application database (CEP cache). |
| Prometheus | `9090` | Scrapes metrics from the app's `/actuator/prometheus`. |
| Grafana | `3000` | Dashboards for metrics and logs (login: `admin` / `admin`). |
| Loki | `3100` | Receives logs pushed directly from the app. |

A note on networking: the application runs **on the host** (via `./mvnw spring-boot:run`), while the stack runs in Docker. Prometheus reaches the host app through `host.docker.internal:8080`, and the app ships logs to Loki at `localhost:3100`. Grafana talks to Prometheus and Loki by service name (`prometheus:9090`, `loki:3100`) since they share the Compose network.

> Note: this Compose setup is for **local development and demonstration**. It is not the production topology — see *Deployment intent* above (Kubernetes for the stateless app, PostgreSQL outside the cluster).

Once up:
- **Grafana** -> `http://localhost:3000`
- **Prometheus** -> `http://localhost:9090`
- Import a Spring Boot dashboard in Grafana and point it at the Prometheus data source; query logs in **Explore** with `{app="zipcodesearch"}`.

---

## Getting Started

### Prerequisites

- Java 21
- Docker (for the database and observability stack)

### Run

```bash
# 1) start the stack (database + observability)
docker compose up -d

# 2) run the application (from the project root)
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

### API documentation

Once running, the interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/zipcodes/{zipcode}` | Look up a single CEP (8 digits, with or without hyphen). |
| `GET` | `/zipcodes/search?street={name}&page={n}` | Fuzzy street search, paginated. |

---

## Testing

The test suite is layered and scoped to where the real risk lives:

- **Repository** (`@DataJpaTest` against real PostgreSQL) — verifies the `pg_trgm` similarity query and JPA auditing, since the Postgres-specific query can't run on H2.
- **Service** (Mockito) — cache-hit vs. provider resolution vs. not-found propagation.
- **Resilience** — retry attempt counts and circuit-breaker state transitions, tested in isolation.
- **Controller** (`@WebMvcTest`) — status codes, parameter validation, and exception handlers.

Coverage is intentionally dimensioned to risk: orchestration and resilience are tested thoroughly; generated mappers and plain DTOs are not.

```bash
./mvnw test
```

---

## Roadmap

- [ ] Additional CEP providers (e.g. BrasilAPI) to exercise the multi-source abstraction.
- [ ] Scheduled revalidation of stored CEP data (e.g. every 30 days).
- [ ] Kubernetes manifests for the stateless app (database stays managed/external).
- [ ] Containerize the application into the Compose stack for a one-command, full-stack startup.

---

## License

This project is part of a personal portfolio.