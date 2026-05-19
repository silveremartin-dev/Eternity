#!/bin/bash
# Launch Eternity II Server and Client
# Force Java 25 for this session
export JAVA_HOME="/c/Program Files/Java/jdk-25"
export PATH="$JAVA_HOME/bin:$PATH"

echo "========================================"
echo "Eternity II Distributed Solver"
echo "Starting Server..."
echo "========================================"

# Ensure compilation before launch
echo "Compiling project..."
mvn compile
if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed."
    exit 1
fi

# Start server in background
mvn exec:java -Dexec.mainClass=org.game.eternity2.server.ServerApp &

# Wait for server to start
echo "Waiting 5 seconds for server to initialize..."
sleep 5

# Start client
echo "Starting Client..."
mvn exec:java -Dexec.mainClass=org.game.eternity2.client.ClientApp
