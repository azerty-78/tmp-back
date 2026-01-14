# ==========================================
# Script d'initialisation automatique du projet (Windows PowerShell)
# ==========================================

Write-Host "🚀 Initialisation du projet template..." -ForegroundColor Cyan
Write-Host ""

# Fonction pour demander une valeur
function Ask-Value {
    param(
        [string]$Prompt,
        [string]$Default = "",
        [string]$VarName
    )
    
    if ($Default) {
        $value = Read-Host "$Prompt [$Default]"
        if ([string]::IsNullOrWhiteSpace($value)) {
            $value = $Default
        }
    } else {
        $value = Read-Host $Prompt
    }
    
    Set-Variable -Name $VarName -Value $value -Scope Script
}

# Demander les informations du projet
Write-Host "📝 Veuillez fournir les informations suivantes :" -ForegroundColor Yellow
Write-Host ""

Ask-Value "Nom du projet (PROJECT_NAME)" "project-name" "PROJECT_NAME"
Ask-Value "Nom de la base de données MongoDB (MONGO_DATABASE)" $PROJECT_NAME "MONGO_DATABASE"
Ask-Value "URL de l'API (APP_BASE_URL)" "http://localhost:8090" "APP_BASE_URL"
Ask-Value "URL du frontend (APP_FRONTEND_URL)" "http://localhost:3000" "APP_FRONTEND_URL"
Ask-Value "Origines autorisées CORS (ALLOWED_ORIGINS)" "$APP_FRONTEND_URL,http://localhost:3001" "ALLOWED_ORIGINS"
Ask-Value "Nom d'utilisateur Docker Hub (optionnel)" "" "DOCKERHUB_USERNAME"

Write-Host ""
Write-Host "⏳ Configuration en cours..." -ForegroundColor Yellow

# Créer les fichiers .env
Write-Host "📄 Création des fichiers .env..." -ForegroundColor Cyan

# Setup BD
if (-not (Test-Path "setup-bd\.env")) {
    Copy-Item "setup-bd\env.example" "setup-bd\.env"
    (Get-Content "setup-bd\.env") -replace "PROJECT_NAME=project-name", "PROJECT_NAME=$PROJECT_NAME" | Set-Content "setup-bd\.env"
    (Get-Content "setup-bd\.env") -replace "MONGO_DATABASE=project-name", "MONGO_DATABASE=$MONGO_DATABASE" | Set-Content "setup-bd\.env"
    Write-Host "  ✅ setup-bd\.env créé" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  setup-bd\.env existe déjà, ignoré" -ForegroundColor Yellow
}

# Setup API
if (-not (Test-Path "setup-api\.env")) {
    Copy-Item "setup-api\env.example" "setup-api\.env"
    
    # Remplacer PROJECT_NAME
    (Get-Content "setup-api\.env") -replace "PROJECT_NAME=project-name", "PROJECT_NAME=$PROJECT_NAME" | Set-Content "setup-api\.env"
    
    # Générer l'URI MongoDB
    $MONGO_URI = "mongodb://root:qwerty87@${PROJECT_NAME}-mongodb:27017/${MONGO_DATABASE}?authSource=admin"
    (Get-Content "setup-api\.env") -replace "SPRING_DATA_MONGODB_URI=.*", "SPRING_DATA_MONGODB_URI=$MONGO_URI" | Set-Content "setup-api\.env"
    
    # Remplacer les URLs
    (Get-Content "setup-api\.env") -replace "APP_BASE_URL=.*", "APP_BASE_URL=$APP_BASE_URL" | Set-Content "setup-api\.env"
    (Get-Content "setup-api\.env") -replace "APP_FRONTEND_URL=.*", "APP_FRONTEND_URL=$APP_FRONTEND_URL" | Set-Content "setup-api\.env"
    (Get-Content "setup-api\.env") -replace "ALLOWED_ORIGINS=.*", "ALLOWED_ORIGINS=$ALLOWED_ORIGINS" | Set-Content "setup-api\.env"
    
    # Docker Hub username (si fourni)
    if ($DOCKERHUB_USERNAME) {
        (Get-Content "setup-api\.env") -replace "DOCKERHUB_USERNAME=.*", "DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME" | Set-Content "setup-api\.env"
    }
    
    Write-Host "  ✅ setup-api\.env créé" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  setup-api\.env existe déjà, ignoré" -ForegroundColor Yellow
}

# Créer les dossiers nécessaires
Write-Host "📁 Création des dossiers..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path "setup-api\uploads\users" | Out-Null
New-Item -ItemType Directory -Force -Path "setup-api\uploads\stock" | Out-Null
New-Item -ItemType Directory -Force -Path "setup-bd\init-scripts" | Out-Null
Write-Host "  ✅ Dossiers créés" -ForegroundColor Green

# Validation
Write-Host ""
Write-Host "🔍 Validation de la configuration..." -ForegroundColor Cyan

# Vérifier que PROJECT_NAME est identique
$BD_PROJECT = (Select-String -Path "setup-bd\.env" -Pattern "^PROJECT_NAME=" | ForEach-Object { $_.Line -replace "PROJECT_NAME=", "" })
$API_PROJECT = (Select-String -Path "setup-api\.env" -Pattern "^PROJECT_NAME=" | ForEach-Object { $_.Line -replace "PROJECT_NAME=", "" })

if ($BD_PROJECT -ne $API_PROJECT -and $BD_PROJECT -and $API_PROJECT) {
    Write-Host "  ❌ ERREUR: PROJECT_NAME différent entre setup-bd et setup-api" -ForegroundColor Red
    exit 1
}

Write-Host "  ✅ Configuration validée" -ForegroundColor Green

Write-Host ""
Write-Host "✨ Initialisation terminée avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Prochaines étapes :" -ForegroundColor Yellow
Write-Host "  1. Vérifiez les fichiers .env dans setup-bd\ et setup-api\"
Write-Host "  2. Lancez './scripts/start.ps1' pour démarrer les services"
Write-Host "  3. Ou utilisez 'make start' si vous avez un Makefile"
Write-Host ""
