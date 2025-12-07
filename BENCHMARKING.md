# Benchmarking Guide

**Authors:** Gemini AI Assistant, Silvère

## Quick Benchmark

Run the simple benchmark (no JMH required):

```bash
# After mvn clean package
java -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.benchmark.SimpleBenchmark
```

This will output:

- Average time per batch
- Total candidates checked
- Throughput (candidates/sec)

## JMH Micro-Benchmarking (Advanced)

JMH dependencies are already in `pom.xml`. To run JMH benchmarks:

### Method 1: Maven (Recommended for Linux/Mac)

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="org.game.eternity2.server.benchmark.KernelBenchmark"
```

### Method 2: Direct Java (Windows)

Build the JMH uberjar first:

```bash
mvn clean package
# Then run with full classpath (complex on Windows, use Method 1 on Linux)
```

### JMH Benchmarks Available

- `KernelBenchmark`: Measures `EternityKernel.checkCandidates()` throughput

## Interpreting Results

**Target metrics:**

- **Throughput**: > 1M candidates/sec (CPU mode)
- **Latency**: < 1ms per batch (1000 candidates)

**With GPU (future):**

- **Throughput**: > 10M candidates/sec (theoretical)
