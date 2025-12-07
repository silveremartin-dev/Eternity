# Eternity II - Roadmap

## ✅ Completed Phases

### Phase 1: Foundations ✅

- [x] Virtual Threads migration
- [x] gRPC + FlatBuffers
- [x] Redis Job Queue & Constraint Cache

### Phase 4: Benchmarking & Monitoring ✅

- [x] JMH Benchmarking (10.18M candidates/sec baseline)
- [x] Prometheus Metrics (Micrometer)
- [x] /metrics, /health, /ready endpoints

### Phase 5: PostgreSQL Database ✅

- [x] Schema: users, puzzles, solutions, config
- [x] DatabaseManager (HikariCP pooling)
- [x] Flyway migrations
- [x] ConfigDAO (database config access)

### Phase 6: Core Optimization ✅

- [x] PiecePrimitive (64-bit packed: ID+edges+rotation)
- [x] BoardPrimitive (primitive arrays)
- [x] PuzzleLoader (TheSil format)

### Phase 7.1: Security ✅

- [x] JWT Authentication (jjwt)
- [x] bcrypt password hashing
- [x] gRPC AuthInterceptor

---

## 🚧 In Progress

### Phase 7.2: Web Client ← NEXT

- [ ] React + Vite setup
- [ ] gRPC-Web integration
- [ ] Responsive UI
- [ ] Puzzle visualization
- [ ] Solving progress display

---

## 📋 Remaining

### Phase 6.2: Puzzle Editor

- [ ] Create/Import custom puzzles
- [ ] Editor UI (JavaFX & Web)
- [ ] Hint system generator

### Phase 7.3: TLS/SSL

- [ ] HTTPS certificates
- [ ] gRPCs secure transport

### Phase 8: Quality

- [ ] I18n (FR, EN, ES, DE)
- [ ] CI/CD pipeline
- [ ] SonarQube quality gates
- [ ] Javadoc completion

---

## Backlog

- [ ] GPU Cluster (TornadoVM)
- [ ] AI/ML Hints
