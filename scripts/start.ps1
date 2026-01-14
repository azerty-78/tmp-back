# ==========================================
# Script de démarrage unifié des services (Windows PowerShell)
# ==========================================

Write-Host "🚀 Démarrage des services..." -ForegroundColor Cyan
Write-Host ""

# Vérifier que les fichiers .env existent
if (-not (Test-Path "setup-bd\.env")) {
    Write-Host "❌ Erreur: setup-bd\.env n'existe pas" -ForegroundColor Red
    Write-Host "   Lancez d'abord: .\scripts\init-project.ps1" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path "setup-api\.env")) {
    Write-Host "❌ Erreur: setup-api\.env n'existe pas" -ForegroundColor Red
    Write-Host "   Lancez d'abord: .\scripts\init-project.ps1" -ForegroundColor Yellow
    exit 1
}

# Charger les variables d'environnement
$bdEnv = Get-Content "setup-bd\.env" | Where-Object { $_ -match "^[^#]" -and $_ -match "=" } | ForEach-Object {
    $key, $value = $_ -split "=", 2
    [PSCustomObject]@{Key = $key; Value = $value}
}

$projectName = ($bdEnv | Where-Object { $_.Key -eq "PROJECT_NAME" }).Value

Write-Host "📦 Création du réseau Docker (si nécessaire)..." -ForegroundColor Yellow
Set-Location setup-bd

# Créer le réseau si il n'existe pas
$NETWORK_NAME = "$projectName-network"
$networkExists = docker network ls | Select-String "$NETWORK_NAME"
if (-not $networkExists) {
    Write-Host "  🔧 Création du réseau $NETWORK_NAME..." -ForegroundColor Cyan
    docker network create "$NETWORK_NAME"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Réseau créé" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Erreur lors de la création du réseau" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
} else {
    Write-Host "  ✓ Réseau $NETWORK_NAME existe déjà" -ForegroundColor Green
}

Write-Host "📦 Démarrage de MongoDB..." -ForegroundColor Yellow
docker-compose up -d

# Attendre que MongoDB soit healthy
Write-Host "⏳ Attente que MongoDB soit prêt..." -ForegroundColor Yellow
$timeout = 60
$counter = 0
while ($counter -lt $timeout) {
    $status = docker-compose ps 2>$null | Select-String "healthy"
    if ($status) {
        Write-Host "✅ MongoDB est prêt" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
    $counter += 2
    Write-Host "." -NoNewline
}

if ($counter -ge $timeout) {
    Write-Host "❌ Timeout: MongoDB n'est pas prêt après ${timeout}s" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Set-Location ..

Write-Host ""
Write-Host "📦 Démarrage de l'API..." -ForegroundColor Yellow
Set-Location setup-api
docker-compose up -d
Set-Location ..

Write-Host ""
Write-Host "✨ Services démarrés avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Statut des services :" -ForegroundColor Cyan
docker ps --filter "name=$projectName" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""
Write-Host "📋 Commandes utiles :" -ForegroundColor Yellow
Write-Host "  - Voir les logs: docker-compose -f setup-bd/docker-compose.yaml -f setup-api/docker-compose.yaml logs -f"
Write-Host "  - Arrêter: .\scripts\stop.ps1"
Write-Host ""
