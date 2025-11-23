Write-Host "Stopping Eternity System..."

# Stop PowerShell windows with specific titles
$processes = Get-Process | Where-Object { $_.MainWindowTitle -like "*EternityServer*" -or $_.MainWindowTitle -like "*EternityClient*" }

if ($processes) {
    $processes | Stop-Process -Force
    Write-Host "Stopped $($processes.Count) processes."
} else {
    Write-Host "No running Eternity processes found."
}

# Note: This might not kill the java processes spawned by mvn if they detached, 
# but usually closing the parent powershell kills the child if not detached.
# If java processes persist, we might need to be more aggressive:
# Stop-Process -Name "java" -ErrorAction SilentlyContinue 
# But that is risky for other java apps.
