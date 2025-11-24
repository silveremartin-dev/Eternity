# Deployment Scripts

Automated deployment scripts for Eternity II across different platforms.

## 📋 Scripts Available

### Local Deployment

| Script | Platform | Description |
|--------|----------|-------------|
| `deploy-local.sh` | Linux/Mac/WSL | Automated local deployment with optional Redis |
| `deploy-local.ps1` | Windows | Same as above for PowerShell |

**Usage:**
```bash
# Linux/Mac
chmod +x scripts/deploy-local.sh
./scripts/deploy-local.sh

# Windows
.\scripts\deploy-local.ps1
```

**Features:**
- ✅ Checks Java 21+ and Maven
- ✅ Prompts for Redis usage
- ✅ Builds project
- ✅ Starts server

---

### Kubernetes Local

| Script | Platform | Description |
|--------|----------|-------------|
| `deploy-k8s-local.sh` | Linux/Mac | Deploy to local K8s (Docker Desktop) |
| `deploy-k8s-local.ps1` | Windows | Same as above for PowerShell |

**Usage:**
```bash
# Linux/Mac
chmod +x scripts/deploy-k8s-local.sh
./scripts/deploy-k8s-local.sh

# Windows
.\scripts\deploy-k8s-local.ps1
```

**Features:**
- ✅ Verifies Docker and kubectl
- ✅ Builds Docker image
- ✅ Deploys Redis, Eternity, HPA
- ✅ Waits for pods to be ready
- ✅ Shows access instructions

---

### Cloud Deployment

| Script | Platform | Description |
|--------|----------|-------------|
| `deploy-cloud.sh` | AWS/GCP/Azure | Template for cloud deployment |

**Usage:**
```bash
# 1. Edit configuration at top of script:
nano scripts/deploy-cloud.sh
# Change: DOCKER_REGISTRY, CLOUD_PROVIDER

# 2. Authenticate with your cloud provider

# 3. Deploy
chmod +x scripts/deploy-cloud.sh
./scripts/deploy-cloud.sh
```

**Supports:**
- AWS (ECR + EKS)
- GCP (GCR + GKE)
- Azure (ACR + AKS)

---

### Health Check

| Script | Platform | Description |
|--------|----------|-------------|
| `healthcheck.sh` | Linux/Mac | Validates running deployment |
| `healthcheck.ps1` | Windows | Same as above for PowerShell |

**Usage:**
```bash
# Linux/Mac
./scripts/healthcheck.sh

# Windows
.\scripts\healthcheck.ps1
```

**Checks:**
- ✅ HTTP endpoint (8080)
- ✅ gRPC endpoint (50051)
- ✅ Redis connection
- ✅ Java process

---

## 🚀 Quick Start

### First Time Setup

**Local (simplest):**
```bash
# Windows
.\scripts\deploy-local.ps1

# Linux/Mac
./scripts/deploy-local.sh
```

**Kubernetes:**
```bash
# 1. Enable Kubernetes in Docker Desktop

# 2. Deploy
# Windows:
.\scripts\deploy-k8s-local.ps1

# Linux/Mac:
./scripts/deploy-k8s-local.sh
```

---

## 🔧 Configuration

### Environment Variables

Scripts use these environment variables (optional):

```bash
# Server configuration
SERVER_PORT=8080
GRPC_PORT=50051

# Redis configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Docker registry (cloud deployment)
DOCKER_REGISTRY=your-registry.io
```

**Set before running:**
```bash
# Linux/Mac
export SERVER_PORT=9000
./scripts/deploy-local.sh

# Windows
$env:SERVER_PORT=9000
.\scripts\deploy-local.ps1
```

---

## 📝 Complete Documentation

For detailed deployment guides, see:
- **[DEPLOYMENT.md](../DEPLOYMENT.md)** - Complete deployment guide
- **[QUICKSTART.md](../QUICKSTART.md)** - 3-minute quick start
- **[README.md](../README.md)** - Project overview

---

## 🐛 Troubleshooting

### Script Permission Denied (Linux/Mac)

```bash
chmod +x scripts/*.sh
```

### PowerShell Execution Policy (Windows)

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Docker Not Found

Install Docker Desktop:
- **Windows/Mac**: https://www.docker.com/products/docker-desktop
- **Linux**: Use your package manager

### Kubernetes Not Available

Enable in Docker Desktop:
- Settings → Kubernetes → Enable Kubernetes

---

## 🎯 Examples

### Deploy locally with Redis

```bash
# Will prompt for Redis
./scripts/deploy-local.sh
# Answer: y
```

### Deploy to K8s and check health

```bash
./scripts/deploy-k8s-local.sh
sleep 10  # Wait for startup
./scripts/healthcheck.sh
```

### Deploy to AWS

```bash
# 1. Configure
export DOCKER_REGISTRY="123456789.dkr.ecr.us-east-1.amazonaws.com"
export CLOUD_PROVIDER="aws"

# 2. Authenticate
aws ecr get-login-password | docker login --username AWS --password-stdin $DOCKER_REGISTRY
aws eks update-kubeconfig --name eternity-cluster

# 3. Deploy
./scripts/deploy-cloud.sh
```

---

**All scripts include error handling and helpful status messages.**
