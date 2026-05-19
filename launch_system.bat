@echo off
REM Launch Eternity II Server and Client for testing
REM This script starts the server and one client instance

REM Force Java 25 for this session
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ========================================
echo Eternity II Distributed Solver
echo Starting Server and Client...
echo ========================================
echo.

REM Ensure compilation before launch
echo Compiling project...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please check your code.
    pause
    exit /b %ERRORLEVEL%
)

REM Start server in new window
echo Starting Server...
start "Eternity Server" cmd /k "cd /d %~dp0 && set \"JAVA_HOME=C:\Program Files\Java\jdk-25\" && mvn exec:java -Dexec.mainClass=org.game.eternity2.server.ServerApp"

REM Wait for server to start
echo Waiting 5 seconds for server to initialize...
timeout /t 5 /nobreak > nul

REM Start client in new window
echo Starting Client...
start "Eternity Client" cmd /k "cd /d %~dp0 && set \"JAVA_HOME=C:\Program Files\Java\jdk-25\" && mvn exec:java -Dexec.mainClass=org.game.eternity2.client.ClientApp"

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
