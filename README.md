# Eternity II - High Performance Architecture

> **Status**: Production-ready architecture with GPU-ready solver, distributed job queue, and Kubernetes deployment manifests.

## 🚀 Quick Start

### Prerequisites
- **Java 21** (Eclipse Temurin recommended)
- **Maven 3.9+**
- **Docker** (for containerization)
- **Kubernetes** (optional, for deployment)

### Build & Run

```bash
# Build the project
mvn clean package

# Run server
java -jar target/eternity-1.0-SNAPSHOT.jar

# Build Docker image
docker build -t eternity-server:latest .

# Deploy to Kubernetes
kubectl apply -f k8s/
```

## 📋 Architecture Overview

### Core Technologies
- **Java 21**: Virtual Threads for high-concurrency
- **gRPC**: High-performance RPC with Protocol Buffers
- **FlatBuffers**: Zero-copy serialization
- **Redis**: Distributed job queue & constraint cache (Lettuce client)
- **Docker**: Multi-stage containerization
- **Kubernetes**: Orchestration with auto-scaling (HPA)

### Project Structure
```
Eternity/
├── src/main/java/org/game/eternity2/
│   ├── server/
│   │   ├── EternityServer.java          # Main server with Virtual Threads
│   │   ├── JobManager.java              # Job orchestration
│   │   ├── grpc/                        # gRPC service implementation
│   │   ├── redis/                       # Redis integrations
│   │   │   ├── RedisJobQueue.java       # Distributed job queue
│   │   │   ├── ConstraintCache.java     # Tile constraint cache
│   │   │   └── RedisConnectionManager.java
│   │   └── kernel/
│   │       └── EternityKernel.java      # GPU-ready parallel solver
│   ├── client/
│   │   ├── AdvancedEternitySolver.java  # Backtracking solver
│   │   └── grpc/                        # gRPC client
│   └── elements/                         # Game logic (Board, Tiles)
├── k8s/
│   ├── redis.yaml                       # Redis StatefulSet
│   ├── eternity.yaml                    # Eternity Deployment
│   └── hpa.yaml                         # Horizontal Pod Autoscaler
├── Dockerfile                           # Multi-stage build
└── docker-compose.yml                   # Dev environment (Redis)
```

## 🎯 Key Features

### 1. Virtual Threads (Java 21)
- **Non-blocking I/O**: Handles 100+ concurrent clients without platform threads
- **Simplified concurrency**: No callback hell
- **Resource efficient**: Low memory footprint

### 2. GPU-Ready Solver
- **Hybrid approach**: CPU manages backtracking tree, GPU evaluates candidates in parallel
- **CPU fallback**: Fully functional without TornadoVM
- **Architecture**: Ready for GPU offload (see `TORNADOVM_SETUP.md`)

### 3. Distributed Job Queue (Redis)
- **LPUSH/BRPOP**: Reliable job distribution
- **Scalable**: Multiple workers can consume from the same queue
- **Constraint Cache**: O(1) lookup for valid tile candidates

### 4. Kubernetes Deployment
- **Auto-scaling**: HPA based on CPU/Memory (1-10 replicas)
- **Stateless server**: Easy horizontal scaling
- **Stateful Redis**: Persistent storage with StatefulSet

## 📊 Performance Targets

| Metric | Baseline | Current | Target (Future) |
|--------|----------|---------|-----------------|
| Throughput | 100 jobs/min | TBD | 500+ jobs/min |
| Latency p99 | 500ms | TBD | <200ms |
| Concurrent Clients | 10 | 100+ | 1000+ |
| Memory Usage | 1GB | ~512MB | <512MB |

## 🔧 Configuration

### Environment Variables
```bash
# Server
SERVER_PORT=8080
GRPC_PORT=50051

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Docker Compose (Development)
```bash
docker-compose up -d  # Starts Redis
```

## 🚢 Deployment

### Local Kubernetes (Docker Desktop)
```bash
# 1. Enable Kubernetes in Docker Desktop

# 2. Deploy
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/eternity.yaml
kubectl apply -f k8s/hpa.yaml

# 3. Verify
kubectl get pods
kubectl get svc
kubectl get hpa

# 4. Access
kubectl port-forward svc/eternity-server 8080:8080
```

### Production Considerations
- **Secrets**: Use Kubernetes Secrets for Redis credentials
- **Monitoring**: Add Prometheus + Grafana
- **Logging**: Centralized logging with ELK/Loki
- **Persistence**: Configure Redis PVC storage class

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests (requires Docker)
mvn verify -DskipITs=false

# Load tests (TODO)
# jmeter -n -t load-test.jmx
```

## 📚 Documentation

- [`TASK.md`](TASK.md): Project task list and progress
- [`TORNADOVM_SETUP.md`](TORNADOVM_SETUP.md): GPU acceleration setup guide
- [`REDIS_SETUP.md`](REDIS_SETUP.md): Redis configuration details
- [`ARCHITECTURE_FINAL.md`](ARCHITECTURE_FINAL.md): Detailed architecture design

## 🗺️ Roadmap

### Completed ✅
- [x] Virtual Threads migration
- [x] gRPC + FlatBuffers integration
- [x] Redis job queue & constraint cache
- [x] GPU-ready solver architecture (CPU fallback)
- [x] Docker containerization
- [x] Kubernetes manifests + HPA

### Future Work 🔮
- [ ] TornadoVM full integration (GPU acceleration)
- [ ] Custom metrics scaling (KEDA for Redis queue depth)
- [ ] Monitoring stack (Prometheus/Grafana)
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Performance benchmarks

## 🤝 Contributing

This is an experimental architecture project. Key areas for contribution:
- Performance benchmarks
- TornadoVM integration
- Solver optimizations
- Documentation improvements

## 📄 License

[Your License Here]

---

**Built with ❤️ using Java 21, gRPC, Redis, and Kubernetes**
