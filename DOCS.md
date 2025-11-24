# Eternity II - Documentation Structure

## 📚 Documentation Index

### 🚀 Quick Start
1. **[QUICKSTART.md](QUICKSTART.md)** - Démarrage en 3 minutes
2. **[README.md](README.md)** - Vue d'ensemble et navigation

### 🎯 Core Documentation
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Résumé technique complet du projet
- **[TASK.md](TASK.md)** - Liste des tâches et progression
- **[ARCHITECTURE_FINAL.md](ARCHITECTURE_FINAL.md)** - Design architectural détaillé
- **[DOCUMENTATION.md](DOCUMENTATION.md)** - Guide de documentation (meta)

### 🚢 Deployment
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Guide de déploiement complet
  - Local (Windows/Linux)
  - Kubernetes local
  - Cloud (AWS/GCP/Azure)
- **[scripts/](scripts/)** - Scripts de déploiement automatisés
  - `deploy-local.sh|.ps1`
  - `deploy-k8s-local.sh|.ps1`
  - `deploy-cloud.sh`
  - `healthcheck.sh|.ps1`

### ⚙️ Setup Guides
- **[REDIS_SETUP.md](REDIS_SETUP.md)** - Configuration Redis
- **[TORNADOVM_SETUP.md](TORNADOVM_SETUP.md)** - GPU acceleration setup
- **[INTEL_GPU_OPENCL.md](INTEL_GPU_OPENCL.md)** - Intel GPU reference

### 🏗️ Technical
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architecture overview
- **[docker-compose.yml](docker-compose.yml)** - Local dev environment
- **[Dockerfile](Dockerfile)** - Container image
- **[k8s/](k8s/)** - Kubernetes manifests
  - `redis.yaml`
  - `eternity.yaml`
  - `hpa.yaml`

### 📦 Archive
- **[archive/](archive/)** - Documentation de travail intermédiaire (obsolète)

---

## 🗂️ Documentation par cas d'usage

### Je veux démarrer rapidement
→ [QUICKSTART.md](QUICKSTART.md)

### Je veux comprendre le projet
→ [README.md](README.md) puis [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

### Je veux déployer
→ [DEPLOYMENT.md](DEPLOYMENT.md) puis `scripts/`

### Je veux l'architecture technique
→ [ARCHITECTURE_FINAL.md](ARCHITECTURE_FINAL.md)

### Je veux configurer le GPU
→ [TORNADOVM_SETUP.md](TORNADOVM_SETUP.md)

### Je veux voir les tâches terminées
→ [TASK.md](TASK.md)

---

## ✅ Documentation à jour

Tous les fichiers listés ci-dessus sont **à jour** et reflètent l'état **production-ready** du projet.

Les fichiers obsolètes ont été déplacés dans `archive/`.
