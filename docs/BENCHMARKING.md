# Benchmarking Guide

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 1. Quick Standalone Benchmark

Run the lightweight benchmark without JMH overhead:

```bash
mvn clean package -DskipTests
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.benchmark.SimpleBenchmark
```

**Measured Outputs:**
- Average evaluation time per batch (1,000 candidates)
- Total candidate states evaluated
- Throughput in candidates / second (**~21,000,000 candidates/sec** per CPU core)

---

## 2. JMH Micro-Benchmarking (Rigorous JIT Profiling)

The project includes Java Microbenchmark Harness (JMH) dependencies in `pom.xml`.

To execute the JMH benchmark suite:

```bash
mvn clean package -DskipTests
mvn exec:java -Dexec.mainClass="org.game.eternity2.server.benchmark.KernelBenchmark"
```

### Benchmarks Included:
- `KernelBenchmark`: Measures raw candidate verification throughput via `EternityKernel.checkCandidates()` across multiple warmup iterations and forks.

---

## 3. Real Solving Times & Resolution Milestones

| Benchmark Case | Board Dimensions | Piece Count | Measured Resolution Time | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Micro 4x4** | $4 \times 4$ | 16 pieces | **< 10 ms** | Cold JVM start, single thread |
| **Training 6x6** | $6 \times 6$ | 36 pieces | **< 50 ms** | Deterministic backtracking |
| **Intermediate 12x6** | $12 \times 6$ | 72 pieces | **~ 3.7 s** | `BasicEternitySolver`, no clues |
| **Full Eternity II 16x16** | $16 \times 16$ | 256 pieces | *Distributed / In progress* | Distributed cluster with clues |

---

© 2026 Silvère Martin-Michiellot & Antigravity
