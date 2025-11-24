#!/bin/bash
# Eternity II - Health Check & Validation Script

echo "🏥 Eternity II - Health Check"
echo "============================="
echo ""

ERRORS=0

# Check Server HTTP
echo "🔍 Checking HTTP endpoint (localhost:8080)..."
if curl -s -f http://localhost:8080 >/dev/null 2>&1; then
    echo "✅ HTTP server responding"
else
    echo "❌ HTTP server not responding"
    ERRORS=$((ERRORS + 1))
fi

# Check gRPC (si grpcurl est installé)
echo ""
echo "🔍 Checking gRPC endpoint (localhost:50051)..."
if command -v grpcurl &> /dev/null; then
    if grpcurl -plaintext localhost:50051 list >/dev/null 2>&1; then
        echo "✅ gRPC server responding"
    else
        echo "❌ gRPC server not responding"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "⚠️  grpcurl not installed, skipping gRPC check"
    echo "   Install: brew install grpcurl (Mac) or go install ..."
fi

# Check Redis (if docker-compose is used)
echo ""
echo "🔍 Checking Redis..."
if docker ps | grep -q eternity.*redis; then
    if docker exec $(docker ps -qf "name=eternity.*redis") redis-cli PING | grep -q PONG; then
        echo "✅ Redis responding"
    else
        echo "❌ Redis not responding"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "⚠️  Redis container not found (in-memory mode OK)"
fi

# Check Java process
echo ""
echo "🔍 Checking Java process..."
if pgrep -f "eternity.*jar" > /dev/null; then
    echo "✅ Eternity server process running"
else
    echo "❌ No Eternity server process found"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "=============================="
if [ $ERRORS -eq 0 ]; then
    echo "✅ All checks passed!"
    exit 0
else
    echo "❌ $ERRORS check(s) failed"
    exit 1
fi
