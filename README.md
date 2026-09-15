# Eternity II Distributed Engine

**Authors:** Silvère Martin-Michiellot & Antigravity (Google DeepMind)  
**Version:** 4.0 (Production Ready)  
**License:** MIT

---

## Overview

A massive-scale, high-performance distributed engine dedicated to solving the notorious **Eternity II** edge-matching puzzle ($16 \times 16$ board with $256! \times 4^{256} \approx 10^{600}$ combinatorial states). Built on modern **Java 25** with preview features, the engine leverages extreme low-level primitive optimizations (64-bit scalar packing), deterministic backtracking, Monte Carlo Tree Search (MCTS), Stochastic Refinement, GPU acceleration via TornadoVM, and a distributed real-time client-server architecture.

---

## Core Features

- **⚡ Lightning-Fast Primitive Engine:** 64-bit scalar tile representations (`PiecePrimitive` & `BoardPrimitive`) enabling $O(1)$ bit-shift rotations and edge matching with zero heap allocations during core search loops.
- **🌐 Distributed Computing Architecture:** Centralized Server distributing workloads and synchronizing master solutions via custom TCP sockets (with strict `ObjectInputFilter` anti-RCE), gRPC + FlatBuffers (Zero-Copy), and WebSockets.
- **🧠 Heterogeneous Solver Engine:**
  - **Iterative MCV Backtracking:** Row-scan with Most Constrained Variable ordering and locked hint pieces map (`isFixed[]`).
  - **Monte Carlo Tree Search (MCTS):** Upper Confidence Bound for Trees (UCT) with stochastic rollouts on available tiles.
  - **GPU Acceleration (TornadoVM):** Batch candidate verification compiled dynamically to OpenCL/PTX GPU kernels.
  - **Stochastic Refinement:** Simulated annealing with unit tile rotations and 2-tile permutations.
- **🔄 Real-Time Synchronization & Telemetry:** Instant broadcasting of `BEST_BOARD_UPDATE` to all cluster nodes, with real-time web dashboard telemetry.
- **🛡️ Enterprise Security & Hardening:** Strict socket deserialization filtering, BCrypt password hashing, JWT authentication (HS256 with cryptographically secure random fallback), and crash-safe atomic persistence.
- **📊 Production Observability:** Native Prometheus `/metrics`, `/health`, and `/ready` probes, along with structured asynchronous Log4j2 logging.
- **🎨 Interactive User Interfaces:** Built-in JavaFX 19 Desktop client & server dashboards with live board visualization, and a responsive WebSocket Web Client.

---

## Performance Benchmarks

| Board Size | Piece Count | Time to First Solution | Strategy |
| :--- | :--- | :--- | :--- |
| **4x4** | 16 pieces | `< 10 ms` | Basic / Scanline Backtracking |
| **6x6** | 36 pieces | `< 50 ms` | Scanline Backtracking |
| **12x6** | 72 pieces | `~ 3.7 s` | Scanline Backtracking |
| **16x16** | 256 pieces | *Distributed / In progress* | Distributed Cluster (Backtracking + MCTS + GPU) |

- **Throughput:** ~21 Million candidate evaluations per second per CPU core.
- **Test Suite:** 85 automated unit and integration tests passing at 100%.

---

## Quick Start

### 1. Prerequisites
- **Java 25+** (with `--enable-preview`)
- **Maven 3.9+**
- **Docker** (optional, for Redis and PostgreSQL)

### 2. Build the Project
```bash
mvn clean package -DskipTests
```

### 3. Run the Server
```bash
# Launch the JavaFX Server Dashboard
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp
```

### 4. Run the Solver Client(s)
```bash
# Launch a JavaFX computation client
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
```

### 5. Access Endpoints
- **TCP Solver:** `localhost:12345`
- **WebSocket Gateway:** `ws://localhost:12346`
- **gRPC Service:** `localhost:12347`
- **Prometheus Metrics:** `http://localhost:12348/metrics`
- **Health Check:** `http://localhost:12348/health`
- **Readiness Check:** `http://localhost:12348/ready`
- **Web Dashboard:** Open `src/main/web-client/index.html` in your browser.

---

## Tech Stack & Architecture

| Layer | Technology | Status |
| :--- | :--- | :--- |
| **Runtime** | Java 25 (`--enable-preview`) | ✅ Active |
| **Concurrency** | Virtual Threads (`Thread.ofVirtual`), Scheduled Pools | ✅ Active |
| **Zero-Copy Serialization** | Google FlatBuffers 23.5.26 | ✅ Active |
| **RPC & Networking** | gRPC 1.60.0 + Protobuf 3.24 + WebSockets | ✅ Active |
| **GPU Acceleration** | TornadoVM 4.0.0-jdk25 (OpenCL / PTX) | ✅ Active (with CPU fallback) |
| **Caching & Queues** | Redis (Lettuce async) + In-Memory Queue | ✅ Active |
| **Persistence** | PostgreSQL + HikariCP + Flyway + Atomic JSON | ✅ Active |
| **Security** | `ObjectInputFilter`, BCrypt, JJWT 0.12.3 | ✅ Active |
| **Metrics & Logs** | Micrometer, Prometheus, Log4j2 async | ✅ Active |
| **Testing** | JUnit 5, Mockito, TestFX, gRPC Testing | ✅ 85 tests passing |

---

## Project Structure

```
eternity/
├── src/main/java/org/game/eternity2/
│   ├── client/          # JavaFX client, statistics poller, worker executor
│   ├── config/          # ConfigurationManager (persistent key-value store)
│   ├── editor/          # PuzzleEditor GUI
│   ├── i18n/            # Internationalization (ResourceBundles)
│   ├── io/              # FlatBuffersSerializer, JsonSolutionPersistence, PuzzleLoaderWriter
│   ├── kernel/          # TornadoVM GPU compute driver & kernels
│   ├── model/           # BoardPrimitive & PiecePrimitive (64-bit zero-allocation)
│   ├── server/          # ServerApp, EternityServer, JobManager, WebSocketServer
│   │   ├── benchmark/   # JMH & standalone benchmark suites
│   │   ├── db/          # DatabaseManager, HikariCP, DAO, Flyway migrations
│   │   ├── grpc/        # EternityServiceImpl (gRPC service)
│   │   ├── monitoring/  # MetricsProvider (Prometheus / Micrometer)
│   │   ├── redis/       # RedisConnectionManager, RedisJobQueue, ConstraintCache
│   │   └── security/    # JwtProvider, AuthInterceptor, PasswordUtils
│   └── solver/          # EternitySolverEngine, MCTSSolver, GPUEternitySolver, HybridSolver
├── src/main/resources/  # Puzzles dataset, i18n bundles, log4j2.xml, schema
├── src/main/web-client/ # HTML5 / CSS3 / Vanilla JS Web Client
├── k8s/                 # Kubernetes deployment manifests & HPA
└── docs/                # Architecture, setup, benchmarks, and showcase posts
```

---

## Documentation Index

- [System Architecture](file:///c:/Silvere/Produits/Developpement/Eternity/ARCHITECTURE.md)
- [Quick Start Guide](file:///c:/Silvere/Produits/Developpement/Eternity/QUICKSTART.md)
- [Deployment Guide (Local, Docker & Kubernetes)](file:///c:/Silvere/Produits/Developpement/Eternity/DEPLOYMENT.md)
- [Benchmarking Guide](file:///c:/Silvere/Produits/Developpement/Eternity/BENCHMARKING.md)
- [Eternity II Game Rules & Mathematical Background](file:///c:/Silvere/Produits/Developpement/Eternity/ETERNITY2_GAME.md)
- [Redis Setup & Clustering](file:///c:/Silvere/Produits/Developpement/Eternity/REDIS_SETUP.md)
- [TLS & Security Configuration](file:///c:/Silvere/Produits/Developpement/Eternity/TLS_SETUP.md)
- [TornadoVM GPU Acceleration Setup](file:///c:/Silvere/Produits/Developpement/Eternity/TORNADOVM_SETUP.md)

---

© 2026 Silvère Martin-Michiellot & Antigravity
