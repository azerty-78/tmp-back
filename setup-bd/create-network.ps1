# Script pour créer le réseau Docker avant de démarrer les services (Windows PowerShell)
# Ce script doit être exécuté avant docker-compose up

# Charger les variables d'environnement
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
}

$PROJECT_NAME = if ($env:PROJECT_NAME) { $env:PROJECT_NAME } else { "project-name" }
$NETWORK_NAME = "$PROJECT_NAME-network"

Write-Host "🔧 Création du réseau Docker: $NETWORK_NAME" -ForegroundColor Cyan

# Vérifier si le réseau existe déjà
$networkExists = docker network ls | Select-String "$NETWORK_NAME"
if ($networkExists) {
    Write-Host "✓ Le réseau $NETWORK_NAME existe déjà" -ForegroundColor Green
} else {
    # Créer le réseau
    docker network create "$NETWORK_NAME"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Réseau $NETWORK_NAME créé avec succès" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de la création du réseau" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "📋 Vous pouvez maintenant lancer: docker-compose up -d" -ForegroundColor Yellow
