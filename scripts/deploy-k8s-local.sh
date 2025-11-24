#!/bin/bash
# Eternity II - Kubernetes Local Deployment (Docker Desktop)

set -e

echo "🚀 Eternity II - Kubernetes Local Deployment"
echo "============================================="

# Vérifier Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Install Docker Desktop"
    exit 1
fi

# Vérifier kubectl
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Enable Kubernetes in Docker Desktop"
    exit 1
fi

# Vérifier que Kubernetes est actif
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Kubernetes cluster not reachable"
    echo "   Enable Kubernetes in Docker Desktop Settings"
    exit 1
fi

echo "✅ Kubernetes cluster detected"

# Build image
echo ""
echo "🔨 Building Docker image..."
docker build -t eternity-server:latest .

if [ $? -eq 0 ]; then
    echo "✅ Image built: eternity-server:latest"
else
    echo "❌ Docker build failed"
    exit 1
fi

# Deploy Redis
echo ""
echo "📦 Deploying Redis..."
kubectl apply -f k8s/redis.yaml

# Wait for Redis
echo "   Waiting for Redis pod..."
kubectl wait --for=condition=ready pod -l app=redis --timeout=60s

# Deploy Eternity
echo ""
echo "📦 Deploying Eternity Server..."
kubectl apply -f k8s/eternity.yaml

# Deploy HPA
echo ""
echo "📦 Deploying HPA..."
kubectl apply -f k8s/hpa.yaml

# Wait for Eternity
echo ""
echo "   Waiting for Eternity pod..."
kubectl wait --for=condition=ready pod -l app=eternity-server --timeout=90s

# Status
echo ""
echo "✅ Deployment complete!"
echo ""
echo "📊 Cluster Status:"
kubectl get pods
echo ""
kubectl get svc
echo ""
kubectl get hpa

echo ""
echo "🌐 Access the service:"
echo "   kubectl port-forward svc/eternity-server 8080:8080"
echo ""
echo "📝 View logs:"
echo "   kubectl logs -f deployment/eternity-server"
