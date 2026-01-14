# ==========================================
# Script d'arrêt des services (Windows PowerShell)
# ==========================================

Write-Host "🛑 Arrêt des services..." -ForegroundColor Yellow
Write-Host ""

Set-Location setup-api
docker-compose down
Set-Location ..

Set-Location setup-bd
docker-compose down
Set-Location ..

Write-Host ""
Write-Host "✅ Services arrêtés" -ForegroundColor Green
