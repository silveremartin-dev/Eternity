# AGENTS.md - Developer & AI Agent Guidelines

> **Project:** Eternity II Distributed Engine (v4.0)  
> **Authors:** Silvère Martin-Michiellot & Antigravity (Google DeepMind)  
> **Runtime:** Java 25 (Preview Features Enabled) | Maven 3.9+  
> **Repository:** `Eternity/v2`

---

## 1. Mission & System Overview

The **Eternity II Distributed Engine** is a high-performance, massive-scale distributed solver for the combinatorial $16 \times 16$ Eternity II edge-matching puzzle ($10^{600}$ state space).

Key components:
- **Zero-Allocation Primitive Core:** Ultra-optimized 64-bit scalar tile representations (`PiecePrimitive` and `BoardPrimitive`).
- **Heterogeneous Solvers:** Deterministic Scanline/MCV Backtracking, Monte Carlo Tree Search (MCTS), Stochastic Refinement / Simulated Annealing, and GPU Acceleration via TornadoVM (OpenCL/PTX).
- **Distributed Master/Worker Cluster:** TCP sockets with strict `ObjectInputFilter`, gRPC + FlatBuffers zero-copy RPC, WebSockets telemetry gateway, Redis job queue, and PostgreSQL persistence.
- **Client & Observability:** JavaFX Desktop GUI client/server dashboards, Web client UI, Prometheus metrics endpoint (`/metrics`), and Log4j2 asynchronous logging.

---

## 2. Environment & Tooling

### Prerequisites
- **Java:** JDK 25+ with `--enable-preview`
- **Build System:** Apache Maven 3.9+
- **OS:** Windows / Linux / macOS
- **Optional Services:** Docker (for Redis and PostgreSQL), TornadoVM 4.0.0-jdk25 (for OpenCL/PTX GPU kernels)

### Essential Commands

```bash
# Clean and build without tests
mvn clean package -DskipTests

# Run full test suite (JUnit 5 + Mockito + TestFX + gRPC)
mvn test

# Run a specific unit test
mvn test -Dtest=PiecePrimitiveTest

# Run Server Dashboard (JavaFX)
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp

# Run Solver Client (JavaFX)
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp

# Start supporting containers (Redis & PostgreSQL)
docker-compose up -d
```

---

## 3. Project Structure & Codebase Map

```
eternity/
├── src/main/java/org/game/eternity2/
│   ├── client/          # JavaFX client UI, statistics poller, worker executor
│   ├── config/          # ConfigurationManager (persistent key-value store)
│   ├── editor/          # PuzzleEditor GUI
│   ├── i18n/            # ResourceBundles & Internationalization
│   ├── io/              # FlatBuffersSerializer, JsonSolutionPersistence, PuzzleLoaderWriter
│   ├── kernel/          # TornadoVM GPU compute kernels & memory drivers
│   ├── model/           # BoardPrimitive & PiecePrimitive (64-bit bit-packed zero-allocation)
│   ├── server/          # ServerApp, EternityServer, JobManager, WebSocketServer
│   │   ├── benchmark/   # JMH & standalone benchmark suites
│   │   ├── db/          # DatabaseManager, HikariCP, DAO, Flyway migrations
│   │   ├── grpc/        # EternityServiceImpl (gRPC endpoint)
│   │   ├── monitoring/  # MetricsProvider (Prometheus / Micrometer)
│   │   ├── redis/       # RedisConnectionManager, RedisJobQueue, ConstraintCache
│   │   └── security/    # JwtProvider, AuthInterceptor, PasswordUtils
│   └── solver/          # EternitySolverEngine, MCTSSolver, GPUEternitySolver, HybridSolver
├── src/main/resources/  # Puzzle sets (.txt/.e2p), i18n bundles, log4j2.xml, schema
├── src/main/web-client/ # HTML5 / CSS3 / Vanilla JS telemetry & live visualizer
├── docs/                # Architecture, setup, benchmarks, and technical documentation
├── k8s/                 # Kubernetes deployment manifests & HPA configs
├── docker-compose.yml   # Multi-service infrastructure orchestration
└── pom.xml              # Maven project object model & dependencies
```

---

## 4. Engineering Rules & Coding Conventions

### A. Performance & Memory Management (Critical)
1. **Zero Heap Allocations in Inner Loops:**
   - The core solving loops in `model/` and `solver/` execute up to $20\text{M}+$ evaluations/sec per core.
   - **NEVER** instantiate new objects (`new Piece(...)`, `new ArrayList<>()`, `new Point(...)`, boxed primitives `Integer`, `Long`) inside search recursion, rollout loops, or hot evaluation paths.
   - Use primitive arrays (`long[]`, `int[]`, `byte[]`), bitwise shift operations, and reusable thread-local / scratch structures.
2. **Scalar Bit-Packing (`PiecePrimitive` / `BoardPrimitive`):**
   - Tile states are encoded in 64-bit scalars.
   - Preserve bitmask layouts and bit-shift arithmetic when modifying primitive models.

### B. Concurrency & Networking
1. **Virtual Threads First:**
   - Leverage `Thread.ofVirtual().start(...)` and `Executors.newVirtualThreadPerTaskExecutor()` for blocking network I/O, socket handlers, and RPC calls.
2. **Thread Safety:**
   - Shared data structures in `server/` and `JobManager` must be concurrent-safe (`ConcurrentHashMap`, `AtomicLong`, `AtomicReference`, lock-free queues, or synchronized blocks where appropriate).
3. **Security & Deserialization:**
   - Always enforce `ObjectInputFilter` on raw Java serialization sockets to protect against RCE vulnerabilities.
   - Validate JWT tokens on gRPC and WebSocket channels using `JwtProvider`.

### C. Java 25 & Code Style
1. **Preview Features:** Always maintain `--enable-preview` compatibility in compiler and runtime arguments.
2. **Modern Java Idioms:** Use pattern matching, records, switch expressions, text blocks, and structured concurrency where suitable, while respecting performance constraints in hot paths.
3. **Logging:** Use `org.apache.logging.log4j.LogManager` / `Logger`. Avoid `System.out.println` in production server code; prefer asynchronous structured logging with proper log levels (`debug`, `info`, `warn`, `error`).
4. **Documentation Integrity:** Maintain Javadoc comments, preserve existing docstrings, and update documentation when introducing public API or architectural changes.

---

## 5. Testing & Verification Requirements

- **Unit Testing:** When adding or modifying solver strategies, data models, or serialization routines, provide comprehensive JUnit 5 tests.
- **Regression Checks:** Ensure all 85+ unit and integration tests pass before submitting changes (`mvn test`).
- **Benchmark Guardrails:** Verify that changes to `PiecePrimitive`, `BoardPrimitive`, or core solver routines do not degrade candidate evaluation throughput.
