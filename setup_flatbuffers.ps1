$ErrorActionPreference = "Stop"

$flatcVersion = "23.5.26"
$downloadUrl = "https://github.com/google/flatbuffers/releases/download/v$flatcVersion/Windows.flatc.binary.zip"
$outputZip = "flatc.zip"
$extractPath = "tools"

Write-Host "Downloading FlatBuffers compiler v$flatcVersion..."
Invoke-WebRequest -Uri $downloadUrl -OutFile $outputZip

Write-Host "Extracting..."
if (!(Test-Path $extractPath)) {
    New-Item -ItemType Directory -Force -Path $extractPath | Out-Null
}
Expand-Archive -Path $outputZip -DestinationPath $extractPath -Force

Write-Host "Cleaning up..."
Remove-Item $outputZip

Write-Host "FlatBuffers compiler installed to $extractPath/flatc.exe"
& "$extractPath/flatc.exe" --version
