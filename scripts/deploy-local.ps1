# Eternity II - Local Deployment (Windows PowerShell)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Eternity II - Local Deployment" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier Java 25+
try {
    $javaVersion = (java -version 2>&1 | Select-String "version" | ForEach-Object { ($_ -replace '.*"(\d+).*', '$1') })
    if ([int]$javaVersion -lt 25) {
        Write-Host "❌ Java 25+ required (found: $javaVersion)" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Java $javaVersion detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please install Java 25+" -ForegroundColor Red
    exit 1
}

# Vérifier Maven
try {
    $null = mvn -version
    Write-Host "✅ Maven detected" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found. Please install Maven 3.9+" -ForegroundColor Red
    exit 1
}

# Option: Avec ou sans Redis
Write-Host ""
$useRedis = Read-Host "Deploy with Redis? (y/N)"
if ($useRedis -eq "y" -or $useRedis -eq "Y") {
    Write-Host ""
    Write-Host "🔧 Starting Redis..." -ForegroundColor Yellow
    try {
        docker-compose up -d
        Write-Host "✅ Redis started on localhost:6379" -ForegroundColor Green
        Start-Sleep -Seconds 2
    } catch {
        Write-Host "❌ Docker not found or failed. Install Docker or run without Redis" -ForegroundColor Red
        exit 1
    }
}

# Build
Write-Host ""
Write-Host "🔨 Building project..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build successful" -ForegroundColor Green
} else {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

# Run with ZGC + Virtual Threads optimizations
Write-Host ""
Write-Host "🚀 Starting Eternity Server..." -ForegroundColor Cyan
Write-Host "   - TCP Solver:         localhost:12345" -ForegroundColor White
Write-Host "   - WebSocket:          ws://localhost:12346" -ForegroundColor White
Write-Host "   - gRPC:               localhost:12347" -ForegroundColor White
Write-Host "   - Prometheus metrics: http://localhost:12348/metrics" -ForegroundColor White
if ($useRedis -eq "y" -or $useRedis -eq "Y") {
    Write-Host "   - Redis:              localhost:6379" -ForegroundColor White
}
Write-Host ""
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

java `
  -XX:+UseZGC `
  -XX:+ZGenerational `
  -Xms512m -Xmx4g `
  --enable-preview `
  -jar target/eternity-1.0-SNAPSHOT.jar
