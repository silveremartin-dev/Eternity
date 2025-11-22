@echo off
REM Launch Eternity II Server and Client for testing
REM This script starts the server and one client instance

echo ========================================
echo Eternity II Distributed Solver
echo Starting Server and Client...
echo ========================================
echo.

REM Start server in new window
echo Starting Server...
start "Eternity Server" cmd /k "cd /d %~dp0 && mvn exec:java -Dexec.mainClass=org.game.eternity2.server.ServerApp"

REM Wait for server to start
echo Waiting 5 seconds for server to initialize...
timeout /t 5 /nobreak > nul

REM Start client in new window
echo Starting Client...
start "Eternity Client" cmd /k "cd /d %~dp0 && mvn exec:java -Dexec.mainClass=org.game.eternity2.client.ClientApp"

echo.
echo ========================================
echo Server and Client launched!
echo.
echo Server window: "Eternity Server"
echo Client window: "Eternity Client"
echo.
echo Press any key to exit this launcher...
echo ========================================
pause > nul
