# Eternity II - Quick Start Guide

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 🚀 Get Started in Under 3 Minutes

### Prerequisites

- ✅ **Java 25+** installed (with `--enable-preview`)
- ✅ **Maven 3.9+** installed
- ✅ **Docker** installed (optional, for Redis and PostgreSQL)

---

### Step 1: Build the Project

```bash
mvn clean package -DskipTests
```

---

### Step 2: Choose Your Execution Mode

#### Mode A: Standalone In-Memory (No Redis Required)

1. **Start the Master Server:**
   ```bash
   java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp
   ```
2. In the Server UI, select a puzzle (e.g., `16x16_eternity2`) and click **Start Server**.
3. **Start One or More Client Solvers:**
   ```bash
   java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
   ```
4. Click **Connect** in the Client UI to begin distributed computation.

---

#### Mode B: Distributed with Redis & Database

1. **Start Infrastructure Services:**
   ```bash
   docker-compose up -d
   ```
2. **Verify Redis is Running:**
   ```bash
   docker exec -it eternity-redis-1 redis-cli PING
   # Output should be: PONG
   ```
3. **Launch Server and Clients:**
   ```bash
   # Server
   java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp

   # Client
   java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
   ```

---

#### Mode C: Kubernetes Cluster (Local / Minikube / Docker Desktop)

```bash
# 1. Build Docker Image
docker build -t eternity-server:latest .

# 2. Apply Kubernetes Manifests
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/eternity.yaml
kubectl apply -f k8s/hpa.yaml

# 3. Check Pod Status
kubectl get pods

# 4. Port-Forward to the Server
kubectl port-forward svc/eternity-server 12345:12345 12346:12346 12347:12347 12348:12348
```

---

## 🧪 Verifying the Deployment

### Test 1: Health & Readiness Endpoints
```bash
curl http://localhost:12348/health
# Response: {"status":"UP"}

curl http://localhost:12348/ready
# Response: {"status":"READY"}
```

### Test 2: Prometheus Metrics Scrape
```bash
curl http://localhost:12348/metrics
# Prometheus text format containing eternity_active_clients, eternity_best_score, etc.
```

### Test 3: Web Dashboard
Open `src/main/web-client/index.html` in any modern web browser to monitor live telemetry, board rendering, and solving throughput.

---

## 🔧 Key Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `12345` | Base TCP solver port |
| `REDIS_HOST` | `localhost` | Redis server hostname |
| `REDIS_PORT` | `6379` | Redis server port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/eternity` | PostgreSQL JDBC connection URL |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(Random 256-bit)* | Secret key for signing JWT tokens |
| `ETERNITY_LANG` | `en` | Default UI language (`en`, `fr`) |

---

© 2026 Silvère Martin-Michiellot & Antigravity
