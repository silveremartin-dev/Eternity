# Eternity II - Health Check & Validation Script (Windows)

Write-Host "🏥 Eternity II - Health Check" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host ""

$Errors = 0

# Check Server HTTP
Write-Host "🔍 Checking HTTP endpoint (localhost:8080)..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 5
    Write-Host "✅ HTTP server responding" -ForegroundColor Green
} catch {
    Write-Host "❌ HTTP server not responding" -ForegroundColor Red
    $Errors++
}

# Check gRPC (basic TCP check)
Write-Host ""
Write-Host "🔍 Checking gRPC port (localhost:50051)..." -ForegroundColor Yellow
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect("localhost", 50051)
    $tcpClient.Close()
    Write-Host "✅ gRPC port is open" -ForegroundColor Green
} catch {
    Write-Host "❌ gRPC port not accessible" -ForegroundColor Red
    $Errors++
}

# Check Redis
Write-Host ""
Write-Host "🔍 Checking Redis..." -ForegroundColor Yellow
$redisContainer = docker ps --filter "name=eternity" --filter "name=redis" --format "{{.ID}}" 2>$null
if ($redisContainer) {
    $redisPing = docker exec $redisContainer redis-cli PING 2>$null
    if ($redisPing -match "PONG") {
        Write-Host "✅ Redis responding" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis not responding" -ForegroundColor Red
        $Errors++
    }
} else {
    Write-Host "⚠️  Redis container not found (in-memory mode OK)" -ForegroundColor Yellow
}

# Check Java process
Write-Host ""
Write-Host "🔍 Checking Java process..." -ForegroundColor Yellow
$javaProcess = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*eternity*jar*" }
if ($javaProcess) {
    Write-Host "✅ Eternity server process running" -ForegroundColor Green
} else {
    Write-Host "❌ No Eternity server process found" -ForegroundColor Red
    $Errors++
}

# Summary
Write-Host ""
Write-Host "==============================" -ForegroundColor Cyan
if ($Errors -eq 0) {
    Write-Host "✅ All checks passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ $Errors check(s) failed" -ForegroundColor Red
    exit 1
}
