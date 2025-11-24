# Eternity II - Project Roadmap (Comprehensive)

## 🎯 Macro Objectives
- **Core**: Solve Eternity II (and custom puzzles) using a distributed client-server architecture.
- **Server**: Java 21 (Virtual Threads), Fault-tolerant, Secure, Scalable.
- **Clients**: Multi-language (Java, JS/Web), Secure connection.
- **Features**: Puzzle Editor, Hints, Automated Verification, Statistics, Dynamic Config.
- **Quality**: CI/CD, Testing, I18n, Logging, Javadoc, Benchmarking.

---

## Phase 1: Foundations (Completed) ✅
- [x] **Concurrency**: Virtual Threads migration.
- [x] **Communication**: gRPC + FlatBuffers.
- [x] **Distribution**: Redis Job Queue & Constraint Cache.

## Phase 5: Data & Configuration (Next) 🗄️
- [ ] **8.1 Database Migration (Replace Properties)**
    - [ ] **Solution**: PostgreSQL (Relational data: Users, Puzzles, Solutions).
    - [ ] Schema: `Users`, `Puzzles` (blob/json), `Solutions` (steps), `Config`.
    - [ ] Migration: Port existing properties to DB.
- [ ] **8.2 Dynamic Configuration**
    - [ ] Store config in DB (hot-reloadable).
    - [ ] API to update config without restart.

---

## Phase 6: Core Refactoring & Optimization ⚡
- [ ] **9.1 Data Structure Optimization**
    - [ ] Refactor `Piece`/`Board` from Objects to **Primitive Arrays/Bitmasks**.
    - [ ] **Format**: `int` based (id + 4 edges + rotation + status).
    - [ ] Import data from `TheSil/edge_puzzle`.
- [ ] **9.2 Puzzle Editor & Generator**
    - [ ] Create/Import custom puzzles (size X*Y).
    - [ ] Editor UI (JavaFX & Web).
    - [ ] Hint system generator.

---

## Phase 7: Security & Web Client 🔒
- [ ] **10.1 Security Hardening**
    - [ ] **TLS/SSL**: Enforce HTTPS/gRPCs (Certificates).
    - [ ] **Auth**: JWT Authentication for clients.
    - [ ] Secure headers & input validation.
- [ ] **10.2 Web Client (JS/TS)**
    - [ ] **Tech**: React/Vue + gRPC-Web.
    - [ ] Responsive UI (Mobile/Desktop).
    - [ ] Visualization of solving progress.

---

## Phase 8: Quality & Industrialization 🏭
- [ ] **11.1 Internationalization (I18n)**
    - [ ] Support: FR, EN, ES, DE.
    - [ ] Resource bundles for Server/Client messages.
- [ ] **11.2 CI/CD Pipeline**
    - [ ] Automated Tests (Unit, Integration, Headless).
    - [ ] Quality Gates (SonarQube, Checkstyle).
    - [ ] Auto-deploy to Staging.
- [ ] **11.3 Documentation & Standards**
    - [ ] Javadoc complete.
    - [ ] Logging standards (Structured Logging).

---

## Backlog / Future
- [ ] GPU Cluster Deployment (TornadoVM).
- [ ] AI/ML Hints generation.
