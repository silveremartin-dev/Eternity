# Eternity II Distributed Solver

**Authors:** Gemini AI Assistant, Silvère  
**Version:** 3.1 (Hybrid Engine)  
**License:** MIT

---

## Overview

High-performance distributed solver for the Eternity II puzzle using modern Java technologies.

## Features
 
 - **Hybrid Solver Engine** (Backtracking + Stochastic)
 - **84M pieces/sec** (Optimized Engine)
 - **Border Pruning** & Parity Checks
 - **Java 21** with Virtual Threads
 - **gRPC + FlatBuffers** for zero-copy communication
 - **PostgreSQL + Redis** for data and caching
 - **Prometheus Metrics** for monitoring
 - **JWT Authentication** for security
 - **Internationalization** (EN, FR, DE, ES)
 - **JavaFX Puzzle Editor** & Designer
 - **Web Client** (HTML/CSS/JS)

## Quick Start

```bash
# Build
mvn clean package -DskipTests

# Start services
docker-compose up -d

# Run server
java -jar target/eternity-1.0-SNAPSHOT.jar

# Generate Javadoc
mvn javadoc:javadoc
```

## Project Structure

```
eternity/
├── src/main/java/         # Java source code
├── src/main/resources/    # Config, i18n, schemas
├── web-client/            # Browser client
├── k8s/                   # Kubernetes manifests
├── scripts/               # Deployment scripts
├── javadoc/               # Generated API docs
└── data/                  # Solutions and user data
```

## Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture |
| [QUICKSTART.md](QUICKSTART.md) | Getting started guide |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Deployment options |
| [TLS_SETUP.md](TLS_SETUP.md) | TLS/SSL configuration |
| [BENCHMARKING.md](BENCHMARKING.md) | Performance testing |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/eternity` | Database |
| `REDIS_HOST` | `localhost` | Redis server |
| `JWT_SECRET` | auto-generated | JWT signing key |
| `ETERNITY_LANG` | `en` | Language (en/fr/de/es) |

## Performance

- **Engine:** 84.5M pieces/sec (CPU Baseline - Optimized)
- **Target:** >200M pieces/sec (GPU)

## Tech Stack

- Java 21 (Virtual Threads, ZGC)
- gRPC + FlatBuffers
- PostgreSQL + HikariCP + Flyway
- Redis (Lettuce client)
- Prometheus + Micrometer
- JavaFX 19
- Kubernetes + Docker

---

© 2026 Silvere Martin-Michiellot & Antigravity
