$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
$BackendDir = Join-Path $RootDir "backend"
$EvidenceDir = Join-Path $RootDir "evidencias"
$LogFile = Join-Path $EvidenceDir "practica03_build.log"

New-Item -ItemType Directory -Force -Path $EvidenceDir | Out-Null

function Write-Section([string]$Title) {
    "" | Tee-Object -FilePath $LogFile -Append
    ("=" * 72) | Tee-Object -FilePath $LogFile -Append
    $Title | Tee-Object -FilePath $LogFile -Append
    ("=" * 72) | Tee-Object -FilePath $LogFile -Append
}

function Invoke-LoggedCommand([string]$Command) {
    Write-Host "`n> $Command" -ForegroundColor Cyan
    "> $Command" | Tee-Object -FilePath $LogFile -Append
    Invoke-Expression "$Command 2>&1" | Tee-Object -FilePath $LogFile -Append
    if ($LASTEXITCODE -ne 0) {
        throw "El comando falló con código $LASTEXITCODE: $Command"
    }
}

"PRACTICA 03 - CONSTRUCCION AUTOMATICA" | Set-Content -Path $LogFile -Encoding UTF8
("Fecha: " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss")) | Add-Content -Path $LogFile -Encoding UTF8

Write-Section "1. VERIFICACION DE JAVA"
Invoke-LoggedCommand "java -version"

if (-not (Test-Path (Join-Path $BackendDir "mvnw.cmd"))) {
    throw "No se encontró backend\mvnw.cmd"
}

Push-Location $BackendDir
try {
    Write-Section "2. VERIFICACION DE MAVEN WRAPPER"
    Invoke-LoggedCommand ".\mvnw.cmd -v"

    Write-Section "3. LIMPIEZA Y COMPILACION"
    Invoke-LoggedCommand ".\mvnw.cmd clean compile"

    Write-Section "4. PRUEBAS AUTOMATIZADAS"
    Invoke-LoggedCommand ".\mvnw.cmd test"

    Write-Section "5. EMPAQUETADO"
    Invoke-LoggedCommand ".\mvnw.cmd clean package"

    Write-Section "6. ARTEFACTO GENERADO"
    $Jars = Get-ChildItem -Path (Join-Path $BackendDir "target") -Filter "*.jar" -File -ErrorAction SilentlyContinue
    if (-not $Jars) {
        throw "La construcción terminó sin localizar un archivo JAR en backend\target."
    }
    $Jars | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize | Out-String | Tee-Object -FilePath $LogFile -Append
}
finally {
    Pop-Location
}

Write-Host "`nProceso finalizado correctamente." -ForegroundColor Green
Write-Host "Evidencia guardada en: $LogFile" -ForegroundColor Green
