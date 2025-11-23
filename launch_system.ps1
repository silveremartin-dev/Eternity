$serverTitle = "EternityServer"
$clientTitle = "EternityClient"

Write-Host "Building project..."
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed. Aborting launch."
    exit
}

Write-Host "Starting Server..."
Start-Process cmd -ArgumentList "/k title EternityServer && mvn exec:java -Dexec.mainClass=org.game.eternity2.server.ServerApp"

Write-Host "Waiting for server to initialize..."
Start-Sleep -Seconds 10

Write-Host "Starting Client 1..."
Start-Process cmd -ArgumentList "/k title EternityClient1 && mvn exec:java -Dexec.mainClass=org.game.eternity2.client.ClientApp"

Write-Host "Starting Client 2..."
Start-Process cmd -ArgumentList "/k title EternityClient2 && mvn exec:java -Dexec.mainClass=org.game.eternity2.client.ClientApp"

Write-Host "System launched."
