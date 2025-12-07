# Eternity II - Deployment Guide

**Authors:** Gemini AI Assistant, Silvère

## 📋 Table of Contents

- [Déploiement Local](#déploiement-local)
- [Déploiement Kubernetes Local](#déploiement-kubernetes-local)
- [Déploiement Cloud](#déploiement-cloud)
- [Configuration par Environnement](#configuration-par-environnement)

---

## 🚀 Déploiement Local

### Prérequis

- Java 21+
- Maven 3.9+
- Docker (optionnel, pour Redis)

### Scripts automatisés

**Linux/Mac/WSL:**

```bash
chmod +x scripts/deploy-local.sh
./scripts/deploy-local.sh
```

**Windows:**

```powershell
.\scripts\deploy-local.ps1
```

### Manuel

**Sans Redis:**

```bash
mvn clean package
java -jar target/eternity-1.0-SNAPSHOT.jar
```

**Avec Redis:**

```bash
docker-compose up -d
mvn clean package
java -jar target/eternity-1.0-SNAPSHOT.jar
```

---

## 🎛️ Déploiement Kubernetes Local

### Prérequis

- Docker Desktop avec Kubernetes activé
- kubectl configuré

### Scripts automatisés

**Linux/Mac:**

```bash
chmod +x scripts/deploy-k8s-local.sh
./scripts/deploy-k8s-local.sh
```

**Windows:**

```powershell
.\scripts\deploy-k8s-local.ps1
```

### Manuel

```bash
# 1. Build image
docker build -t eternity-server:latest .

# 2. Deploy
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/eternity.yaml
kubectl apply -f k8s/hpa.yaml

# 3. Access
kubectl port-forward svc/eternity-server 8080:8080
```

### Vérification

```bash
# Pods status
kubectl get pods

# HPA status
kubectl get hpa

# Logs
kubectl logs -f deployment/eternity-server
```

---

## ☁️ Déploiement Cloud

### Plateformes supportées

| Provider | Registry | Kubernetes |
|----------|----------|------------|
| **AWS** | ECR | EKS |
| **GCP** | GCR | GKE |
| **Azure** | ACR | AKS |

### Template de déploiement

**Étapes:**

1. **Configurer le script `scripts/deploy-cloud.sh`:**

   ```bash
   # Modifier ces variables:
   DOCKER_REGISTRY="your-registry.io"
   CLOUD_PROVIDER="aws"  # ou gcp, azure
   ```

2. **Configurer l'authentification:**

   **AWS (ECR + EKS):**

   ```bash
   # Login ECR
   aws ecr get-login-password --region us-east-1 | \
     docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
   
   # Configure kubectl
   aws eks update-kubeconfig --name eternity-cluster --region us-east-1
   ```

   **GCP (GCR + GKE):**

   ```bash
   # Login GCR
   gcloud auth configure-docker
   
   # Configure kubectl
   gcloud container clusters get-credentials eternity-cluster --zone us-central1-a
   ```

   **Azure (ACR + AKS):**

   ```bash
   # Login ACR
   az acr login --name yourregistry
   
   # Configure kubectl
   az aks get-credentials --resource-group eternity-rg --name eternity-cluster
   ```

3. **Déployer:**

   ```bash
   chmod +x scripts/deploy-cloud.sh
   ./scripts/deploy-cloud.sh
   ```

### Avec GPU (NVIDIA)

**Manifests avec GPU:**

Créer `k8s/eternity-gpu.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eternity-server-gpu
spec:
  replicas: 2
  selector:
    matchLabels:
      app: eternity-server
      tier: gpu
  template:
    metadata:
      labels:
        app: eternity-server
        tier: gpu
    spec:
      containers:
      - name: eternity-server
        image: your-registry/eternity-server:latest
        resources:
          limits:
            nvidia.com/gpu: 1  # 1 GPU par pod
            memory: 4Gi
            cpu: 2000m
          requests:
            memory: 2Gi
            cpu: 1000m
        env:
        - name: ENABLE_GPU
          value: "true"
```

**Déployer avec GPU:**

```bash
# Installer GPU operator (une fois par cluster)
kubectl apply -f https://raw.githubusercontent.com/NVIDIA/gpu-operator/master/deployments/gpu-operator.yaml

# Deploy
kubectl apply -f k8s/eternity-gpu.yaml
```

---

## ⚙️ Configuration par Environnement

### Variables d'environnement

**Fichier `.env` (local):**

```bash
SERVER_PORT=8080
GRPC_PORT=50051
REDIS_HOST=localhost
REDIS_PORT=6379
LOG_LEVEL=INFO
```

**Kubernetes ConfigMap:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: eternity-config
data:
  SERVER_PORT: "8080"
  GRPC_PORT: "50051"
  LOG_LEVEL: "INFO"
---
apiVersion: v1
kind: Secret
metadata:
  name: eternity-secret
type: Opaque
stringData:
  REDIS_PASSWORD: "your-secure-password"
```

**Appliquer:**

```bash
kubectl apply -f k8s/config.yaml
```

**Référencer dans Deployment:**

```yaml
spec:
  containers:
  - name: eternity-server
    envFrom:
    - configMapRef:
        name: eternity-config
    - secretRef:
        name: eternity-secret
```

---

## 🔄 CI/CD

### GitHub Actions (déjà configuré)

Le fichier `.github/workflows/ci-cd.yml` gère:

- ✅ Build automatique sur push
- ✅ Tests
- ✅ Build Docker image
- ⏸️ Push vers registry (à configurer)

**Activer le push vers registry:**

Ajouter des secrets GitHub:

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- Ou `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` pour ECR

---

## 🐛 Troubleshooting

### Problème: Image pull failed

```bash
# Vérifier que l'image existe
docker images | grep eternity-server

# Re-build
docker build -t eternity-server:latest .
```

### Problème: Pods en CrashLoopBackOff

```bash
# Voir les logs
kubectl logs deployment/eternity-server

# Vérifier les events
kubectl describe pod <pod-name>
```

### Problème: HPA ne scale pas

```bash
# Vérifier metrics-server
kubectl get apiservice v1beta1.metrics.k8s.io -o yaml

# Installer metrics-server si absent
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## 📊 Monitoring

### Prometheus + Grafana (optionnel)

```bash
# Installer Prometheus Operator
helm install prometheus prometheus-community/kube-prometheus-stack

# Expose Grafana
kubectl port-forward svc/prometheus-grafana 3000:80
# Login: admin / prom-operator
```

---

## 📚 Références

- [QUICKSTART.md](QUICKSTART.md) - Démarrage rapide
- [README.md](README.md) - Vue d'ensemble
- [k8s/](k8s/) - Manifests Kubernetes
- [scripts/](scripts/) - Scripts de déploiement
