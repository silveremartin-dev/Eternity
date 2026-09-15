# Deployment & Operational Scripts

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

Automated operational scripts for building, running, deploying, and checking the health of Eternity II.

---

## 📋 Available Scripts

### 1. Local Deployment
| Script | Platform | Description |
| :--- | :--- | :--- |
| `deploy-local.sh` | Linux / macOS / WSL | Automated local compilation & launch with optional Redis |
| `deploy-local.ps1` | Windows PowerShell | Same as above for Windows environments |

**Usage:**
```bash
# Linux / macOS
chmod +x scripts/deploy-local.sh
./scripts/deploy-local.sh

# Windows PowerShell
.\scripts\deploy-local.ps1
```

---

### 2. Kubernetes Local Deployment
| Script | Platform | Description |
| :--- | :--- | :--- |
| `deploy-k8s-local.sh` | Linux / macOS | Deploy to local Kubernetes (Docker Desktop / Minikube) |
| `deploy-k8s-local.ps1` | Windows PowerShell | Same as above for Windows PowerShell |

**Usage:**
```bash
# Linux / macOS
chmod +x scripts/deploy-k8s-local.sh
./scripts/deploy-k8s-local.sh

# Windows PowerShell
.\scripts\deploy-k8s-local.ps1
```

---

### 3. Cloud Deployment Template
| Script | Platform | Description |
| :--- | :--- | :--- |
| `deploy-cloud.sh` | AWS / GCP / Azure | Deployment template for EKS, GKE, or AKS clusters |

---

### 4. Health & Diagnostics
| Script | Platform | Description |
| :--- | :--- | :--- |
| `healthcheck.sh` | Linux / macOS | Validates TCP, WebSocket, gRPC, and HTTP endpoints |
| `healthcheck.ps1` | Windows PowerShell | Same as above for Windows PowerShell |

**Target Ports Checked:**
- `12345` (TCP Solver)
- `12346` (WebSocket Server)
- `12347` (gRPC Server)
- `12348` (Prometheus Metrics & Health API)

---

© 2026 Silvère Martin-Michiellot & Antigravity
