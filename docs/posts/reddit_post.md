# 🌌 Building a Distributed, GPU-Accelerated Eternity II Solver in Java 25 (Bitwise Primitives, FlatBuffers/gRPC, MCTS & TornadoVM)

**Subreddit suggestions:** `r/java`, `r/programming`, `r/algorithms`, `r/computerscience`

---

### **Title:**
> *I built a distributed, GPU-accelerated solver for Eternity II in Java 25 (Bitwise math, Zero-Copy FlatBuffers/gRPC, MCTS, and TornadoVM)*

---

### **Post Content:**

Hey r/java / r/programming!

I've been working on solving the infamous **Eternity II** puzzle—a notorious edge-matching puzzle on a $16 \times 16$ board with $256! \times 4^{256} \approx 10^{600}$ potential states. 

Rather than just building a naive backtracking script, I treated this as a full-scale exercise in **extreme performance engineering, hardware acceleration, and distributed systems architecture in modern Java 25**.

Here is a breakdown of how the engine is built under the hood:

---

### 1. Zero-Allocation Bitwise Primitive Engine
At this scale, allocating Java objects per candidate move destroys throughput due to GC pressure. 
- **64-bit Scalar Packing:** Every tile is represented as a single `long` primitive holding `[id (16b) | top (8b) | right (8b) | bottom (8b) | left (8b) | rotation (8b)]`.
- **$O(1)$ Bit-Shift Rotations:** Clockwise rotation is a simple bit-shift & mask operation with zero heap allocations.
- **Lookup Pruning:** `NeighborIndex` and `GlobalPruner` lookups use bit-vectors to prune impossible candidates before expanding tree branches.

---

### 2. Multi-Paradigm Solvers (CPU + GPU)
The engine integrates several modular solvers running concurrently:
1. **Iterative MCV Backtracking:** Row-scanning with Most Constrained Variable ordering, augmented with an `isFixed[]` map to lock hint tiles (such as the central clue) in place.
2. **Monte Carlo Tree Search (MCTS):** Uses Upper Confidence Bound for Trees ($UCT = \bar{X} + \sqrt{2 \ln(N)/n}$) with fast stochastic rollouts.
3. **Stochastic Refinement (Simulated Annealing):** Performs 1-tile rotations and 2-tile permutations to escape local minima.
4. **GPU Offloading via TornadoVM:** Compiles candidate matching kernels directly to OpenCL/PTX for parallel evaluation on GPU compute cores.

---

### 3. Distributed Architecture & Zero-Copy Networking
To scale the compute across multiple machines:
- **gRPC + FlatBuffers:** Replaced JSON/Java Serialization with FlatBuffers over HTTP/2. FlatBuffers provides zero-copy deserialization directly out of network buffers.
- **Redis Work Stealing:** Jobs are queued in Redis via Lettuce (`LPUSH` / `BRPOP`) with an asynchronous Constraint Cache for intermediate pruning states.
- **Real-Time WebSocket & Dashboard:** A WebSocket server broadcasts real-time cluster telemetry (pieces placed/sec, top board scores) to a responsive web dashboard.

---

### 4. Security & Production Hardening
Since remote clients connect to the solver cluster:
- **Strict Anti-RCE Deserialization:** Socket connections implement an explicit `ObjectInputFilter` whitelist.
- **Security & DB:** Passwords hashed with BCrypt, API authenticated via JWT (HS256 with in-memory secure random key generation), and relational configs managed via HikariCP connection pooling + Flyway migrations.
- **Observability:** Native `/metrics` endpoint exporting Prometheus counters, along with `/health` and `/ready` probes.

---

### 📊 Results & Tech Stack
- **Language:** Java 25 (`--enable-preview`)
- **Build & Tests:** Maven, JUnit 5 (85 automated tests, 100% passing)
- **Frameworks:** gRPC, FlatBuffers, TornadoVM, Lettuce (Redis), JavaFX, Log4j2, Micrometer/Prometheus

Check out the full repository here: **[GitHub Repository Link]**

I’d love to hear your feedback, algorithmic ideas, or suggestions on optimizing heuristic search in combinatorics! 🚀
