# Implementation Plan - Eternity II (Phase 4-8)

## Goal Description
Implement the comprehensive roadmap for the Eternity II solver, transforming it into a production-grade, distributed, multi-client system with advanced observability, security, and optimization.

## User Review Required
> [!IMPORTANT]
> **Database Choice**: We are proceeding with **PostgreSQL** for relational data (Users, Puzzles, Solutions).
> **Monitoring Stack**: We are using **Prometheus + Grafana**.
> **Web Client**: We will use **React** with **gRPC-Web**.

## Proposed Changes

### Phase 4: Observability & Benchmarking
#### [NEW] [KernelBenchmark.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/server/benchmark/KernelBenchmark.java)
- Micro-benchmark for `EternityKernel`.
- Metric: Candidates checked per second.

#### [NEW] [MonitoringConfig.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/server/monitoring/MonitoringConfig.java)
- Setup Micrometer registry.
- Expose `/metrics` endpoint.

### Phase 5: Data & Configuration
#### [NEW] [DatabaseManager.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/server/db/DatabaseManager.java)
- Connection pool (HikariCP).
- Flyway/Liquibase for schema migration.

#### [MODIFY] [EternityServer.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/server/EternityServer.java)
- Load config from DB instead of properties.

### Phase 6: Core Refactoring
#### [MODIFY] [Piece.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/model/Piece.java)
- Convert to `record` or primitive wrapper.
- Optimize memory layout.

#### [NEW] [PuzzleEditor](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/client/editor/PuzzleEditor.java)
- JavaFX UI for creating puzzles.

### Phase 7: Security
#### [NEW] [SecurityInterceptor.java](file:///c:/Silvere/Encours/Developpement/Eternity/src/main/java/org/game/eternity2/server/security/SecurityInterceptor.java)
- gRPC interceptor for JWT validation.

## Verification Plan

### Automated Tests
- **Benchmarks**: Run `mvn exec:java ...` to verify performance improvements.
- **Integration**: Test full solving flow with DB and Redis.
- **Load Test**: Simulate 100 clients connecting and requesting jobs.

### Manual Verification
- **Grafana**: Verify dashboards show real-time metrics.
- **Web Client**: Verify solving visualization works in browser.
