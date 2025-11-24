#!/bin/bash
# Eternity II - Local Deployment (Linux/Mac/WSL)

set -e

echo "🚀 Eternity II - Local Deployment"
echo "=================================="

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21+ required (found: $JAVA_VERSION)"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Vérifier Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.9+"
    exit 1
fi

echo "✅ Maven detected"

# Option: Avec ou sans Redis
read -p "Deploy with Redis? (y/N): " USE_REDIS
USE_REDIS=${USE_REDIS:-N}

if [[ "$USE_REDIS" =~ ^[Yy]$ ]]; then
    echo ""
    echo "🔧 Starting Redis..."
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker not found. Install Docker or run without Redis"
        exit 1
    fi
    docker-compose up -d
    echo "✅ Redis started on localhost:6379"
    sleep 2
fi

# Build
echo ""
echo "🔨 Building project..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi

# Run
echo ""
echo "🚀 Starting Eternity Server..."
echo "   - HTTP: http://localhost:8080"
echo "   - gRPC: localhost:50051"
if [[ "$USE_REDIS" =~ ^[Yy]$ ]]; then
    echo "   - Redis: localhost:6379"
fi
echo ""
echo "Press Ctrl+C to stop"
echo ""

java -jar target/eternity-1.0-SNAPSHOT.jar
