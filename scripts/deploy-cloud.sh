#!/bin/bash
# Eternity II - Cloud Deployment Template (AWS/GCP/Azure)
# Customize this script for your cloud provider

set -e

# Configuration
PROJECT_NAME="eternity-server"
DOCKER_REGISTRY="your-registry.io"  # Change to your registry (ECR, GCR, ACR)
K8S_NAMESPACE="eternity"
CLOUD_PROVIDER="aws"  # Options: aws, gcp, azure

echo "🚀 Eternity II - Cloud Deployment ($CLOUD_PROVIDER)"
echo "===================================================="

# Check prerequisites
command -v kubectl >/dev/null 2>&1 || { echo "❌ kubectl required"; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "❌ docker required"; exit 1; }

# Build & Push Image
echo ""
echo "🔨 Building image..."
docker build -t $DOCKER_REGISTRY/$PROJECT_NAME:latest .
docker tag $DOCKER_REGISTRY/$PROJECT_NAME:latest $DOCKER_REGISTRY/$PROJECT_NAME:$(git rev-parse --short HEAD)

echo ""
echo "📦 Pushing to registry..."
# Uncomment based on provider:
# AWS: $(aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $DOCKER_REGISTRY)
# GCP: gcloud auth configure-docker
# Azure: az acr login --name yourregistry

docker push $DOCKER_REGISTRY/$PROJECT_NAME:latest
docker push $DOCKER_REGISTRY/$PROJECT_NAME:$(git rev-parse --short HEAD)

# Create namespace
echo ""
echo "📝 Creating namespace..."
kubectl create namespace $K8S_NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Deploy with customized manifests
echo ""
echo "📦 Deploying to Kubernetes..."

# Update image in manifests
sed -i.bak "s|image: eternity-server:latest|image: $DOCKER_REGISTRY/$PROJECT_NAME:latest|g" k8s/eternity.yaml

kubectl apply -f k8s/redis.yaml -n $K8S_NAMESPACE
kubectl apply -f k8s/eternity.yaml -n $K8S_NAMESPACE
kubectl apply -f k8s/hpa.yaml -n $K8S_NAMESPACE

# Restore backup
mv k8s/eternity.yaml.bak k8s/eternity.yaml

echo ""
echo "⏳ Waiting for pods..."
kubectl wait --for=condition=ready pod -l app=redis -n $K8S_NAMESPACE --timeout=120s
kubectl wait --for=condition=ready pod -l app=eternity-server -n $K8S_NAMESPACE --timeout=120s

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📊 Status:"
kubectl get pods -n $K8S_NAMESPACE
kubectl get svc -n $K8S_NAMESPACE
kubectl get hpa -n $K8S_NAMESPACE

echo ""
echo "🌐 Access (LoadBalancer external IP):"
kubectl get svc eternity-server -n $K8S_NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
echo ""

echo ""
echo "📝 Useful commands:"
echo "   kubectl logs -f deployment/eternity-server -n $K8S_NAMESPACE"
echo "   kubectl describe hpa eternity-server-hpa -n $K8S_NAMESPACE"
echo "   kubectl scale deployment eternity-server --replicas=5 -n $K8S_NAMESPACE"
