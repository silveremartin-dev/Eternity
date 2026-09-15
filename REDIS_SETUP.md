# Redis Distributed Setup for Eternity II

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

Start Redis and Redis Commander:

```bash
docker-compose up -d
```

**Services Started:**
- **Redis Server:** `localhost:6379`
- **Redis Commander (Web GUI):** `http://localhost:8081`

To stop services:
```bash
docker-compose down
```

---

### Option 2: Local / Native Installation

- **Linux (Debian/Ubuntu):** `sudo apt update && sudo apt install redis-server`
- **macOS:** `brew install redis && brew services start redis`
- **Windows:** Run via WSL2 or Docker container.

---

## ⚙️ Redis Integration in Eternity II

The system connects via Lettuce asynchronous client (`io.lettuce:lettuce-core`):

### 1. Distributed Job Queue (`eternity:jobs:pending`)
- **Enqueue (Master Node):** `LPUSH eternity:jobs:pending <job_json>`
- **Dequeue (Worker Node):** `BRPOP eternity:jobs:pending <timeout>` (Atomic FIFO work stealing)
- **Size:** `LLEN eternity:jobs:pending`

### 2. Constraint Cache (`ConstraintCache`)
- Caches partial candidate evaluations to avoid duplicate branch exploration across workers.

---

## 🛠️ CLI Operations & Monitoring

```bash
# Connect to Redis CLI
docker exec -it eternity-redis-1 redis-cli

# Check queue size
LLEN eternity:jobs:pending

# Inspect jobs
LRANGE eternity:jobs:pending 0 10

# Clear queue if necessary
DEL eternity:jobs:pending
```

---

© 2026 Silvère Martin-Michiellot & Antigravity
