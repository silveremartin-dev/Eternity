# Eternity II - Kubernetes Local Deployment (Docker Desktop - Windows)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Eternity II - Kubernetes Local Deployment" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Vérifier Docker
try {
    $null = docker version
    Write-Host "✅ Docker detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker not found. Install Docker Desktop" -ForegroundColor Red
    exit 1
}

# Vérifier kubectl
try {
    $null = kubectl version --client
    Write-Host "✅ kubectl detected" -ForegroundColor Green
} catch {
    Write-Host "❌ kubectl not found. Enable Kubernetes in Docker Desktop" -ForegroundColor Red
    exit 1
}

# Vérifier cluster
try {
    $null = kubectl cluster-info 2>$null
    Write-Host "✅ Kubernetes cluster detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Kubernetes cluster not reachable" -ForegroundColor Red
    Write-Host "   Enable Kubernetes in Docker Desktop Settings" -ForegroundColor Yellow
    exit 1
}

# Build image
Write-Host ""
Write-Host "🔨 Building Docker image..." -ForegroundColor Yellow
docker build -t eternity-server:latest .

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Image built: eternity-server:latest" -ForegroundColor Green
} else {
    Write-Host "❌ Docker build failed" -ForegroundColor Red
    exit 1
}

# Deploy Redis
Write-Host ""
Write-Host "📦 Deploying Redis..." -ForegroundColor Yellow
kubectl apply -f k8s/redis.yaml

Write-Host "   Waiting for Redis pod..." -ForegroundColor Gray
kubectl wait --for=condition=ready pod -l app=redis --timeout=60s

# Deploy Eternity
Write-Host ""
Write-Host "📦 Deploying Eternity Server..." -ForegroundColor Yellow
kubectl apply -f k8s/eternity.yaml

# Deploy HPA
Write-Host ""
Write-Host "📦 Deploying HPA..." -ForegroundColor Yellow
kubectl apply -f k8s/hpa.yaml

# Wait for Eternity
Write-Host ""
Write-Host "   Waiting for Eternity pod..." -ForegroundColor Gray
kubectl wait --for=condition=ready pod -l app=eternity-server --timeout=90s

# Status
Write-Host ""
Write-Host "✅ Deployment complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Cluster Status:" -ForegroundColor Cyan
kubectl get pods
Write-Host ""
kubectl get svc
Write-Host ""
kubectl get hpa

Write-Host ""
Write-Host "🌐 Access the service:" -ForegroundColor Cyan
Write-Host "   kubectl port-forward svc/eternity-server 8080:8080" -ForegroundColor White
Write-Host ""
Write-Host "📝 View logs:" -ForegroundColor Cyan
Write-Host "   kubectl logs -f deployment/eternity-server" -ForegroundColor White
