# Eternity II - Deployment Guide

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## Table of Contents

1. [Local Deployment](#local-deployment)
2. [Docker Deployment](#docker-deployment)
3. [Kubernetes Deployment](#kubernetes-deployment)
4. [Monitoring & Observability](#monitoring--observability)
5. [Environment Variables Reference](#environment-variables-reference)

---

## 1. Local Deployment

### Prerequisites
- **Java 25+** (with `--enable-preview`)
- **Maven 3.9+**

### Automated Deployment Scripts

**Linux / macOS / WSL:**
```bash
chmod +x scripts/deploy-local.sh
./scripts/deploy-local.sh
```

**Windows (PowerShell):**
```powershell
.\scripts\deploy-local.ps1
```

### Manual Build & Run
```bash
# Build JAR
mvn clean package -DskipTests

# Run Server Node
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp

# Run Solver Client Node
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
```

---

## 2. Docker Deployment

### Run Supporting Infrastructure (Redis + PostgreSQL)
```bash
docker-compose up -d
```

To verify container health:
```bash
docker-compose ps
```

### Build and Run Eternity Server in Docker
```bash
# Build image
docker build -t eternity-server:latest .

# Run container
docker run -d \
  --name eternity-server \
  -p 12345:12345 \
  -p 12346:12346 \
  -p 12347:12347 \
  -p 12348:12348 \
  -e REDIS_HOST=host.docker.internal \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/eternity \
  eternity-server:latest
```

---

## 3. Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (Minikube, K3s, Docker Desktop, or GKE/EKS/AKS)
- `kubectl` configured

### Deployment Manifests
```bash
# 1. Deploy Redis State
kubectl apply -f k8s/redis.yaml

# 2. Deploy Server Application & Service
kubectl apply -f k8s/eternity.yaml

# 3. Deploy Horizontal Pod Autoscaler
kubectl apply -f k8s/hpa.yaml
```

### Verification & Scaling
```bash
# Check pod status
kubectl get pods -l app=eternity-server

# Check HPA scaling metrics
kubectl get hpa eternity-server-hpa

# Forward ports for local access
kubectl port-forward svc/eternity-server 12345:12345 12346:12346 12347:12347 12348:12348
```

---

## 4. Monitoring & Observability

### Prometheus Configuration
Add the following scrape target to your `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'eternity-server'
    metrics_path: '/metrics'
    static_configs:
      - targets: ['localhost:12348']
```

### Health & Readiness Probes
- **Liveness Probe:** `GET http://<host>:12348/health` (HTTP 200 OK)
- **Readiness Probe:** `GET http://<host>:12348/ready` (HTTP 200 OK)

---

## 5. Environment Variables Reference

| Variable | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `SERVER_PORT` | `int` | `12345` | Master server TCP listening port |
| `REDIS_HOST` | `string` | `localhost` | Redis server hostname for distributed queues |
| `REDIS_PORT` | `int` | `6379` | Redis server port |
| `DB_ENABLED` | `boolean` | `true` | Enable/disable PostgreSQL storage |
| `DB_URL` | `string` | `jdbc:postgresql://localhost:5432/eternity` | PostgreSQL connection string |
| `DB_USER` | `string` | `postgres` | Database username |
| `DB_PASSWORD` | `string` | `postgres` | Database password |
| `JWT_SECRET` | `string` | *(Auto-generated 256-bit)* | Secret key for JWT token signing |
| `ETERNITY_LANG` | `string` | `en` | Interface language (`en`, `fr`) |

---

© 2026 Silvère Martin-Michiellot & Antigravity
