# Redis Setup for Eternity II

**Authors:** Gemini AI Assistant, Silvère

## Quick Start

### Option 1: Docker Compose (Recommended)

Start Redis with Redis Commander UI:

```bash
docker-compose up -d
```

This will start:

- **Redis** on port `6379` (with data persistence)
- **Redis Commander** UI on port `8081` (<http://localhost:8081>)

To stop:

```bash
docker-compose down
```

To view logs:

```bash
docker-compose logs -f redis
```

### Option 2: Manual Redis Installation

If you don't have Docker, install Redis manually:

- **Windows**: Use WSL2 or download Redis for Windows
- **macOS**: `brew install redis` then `redis-server`
- **Linux**: `sudo apt install redis-server` or equivalent

## Using Redis Job Queue

The `RedisJobQueue` class provides distributed job management:

### Configuration

Jobs are stored in Redis list: `eternity:jobs:pending`

### Operations

- **Enqueue**: `LPUSH eternity:jobs:pending <job_json>`
- **Dequeue**: `BRPOP eternity:jobs:pending <timeout>`
- **Size**: `LLEN eternity:jobs:pending`

### Redis CLI Commands

Monitor the queue:

```bash
# Connect to Redis
redis-cli

# Check queue size
LLEN eternity:jobs:pending

# View all jobs (non-destructive)
LRANGE eternity:jobs:pending 0 -1

# Clear queue (CAUTION!)
DEL eternity:jobs:pending
```

## Architecture

```
┌─────────────┐
│   Server 1  │──┐
└─────────────┘  │
                 │    ┌──────────────┐
┌─────────────┐  ├───▶│    Redis     │
│   Server 2  │──┤    │  Job Queue   │
└─────────────┘  │    └──────────────┘
                 │            │
┌─────────────┐  │            ▼
│   Server N  │──┘    ┌──────────────┐
└─────────────┘       │   Clients    │
                      └──────────────┘
```

## Benefits

✅ **Distributed**: Multiple servers can push/poll jobs
✅ **Persistent**: Jobs survive server restarts
✅ **Atomic**: BRPOP is atomic (no race conditions)
✅ **Scalable**: Redis handles millions of ops/sec

## Next Steps

- [ ] Migrate `JobManager` to use `RedisJobQueue`
- [ ] Implement result queue for completed jobs
- [ ] Add distributed locking for job status updates
- [ ] Setup Redis Cluster for high availability
