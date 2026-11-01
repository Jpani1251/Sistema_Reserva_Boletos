# ==============================================================================
# SCRIPT DE LIMPIEZA DE RECURSOS EN AZURE
# ==============================================================================

Write-Host "⚠️ ATENCIÓN: Esto eliminará todos los recursos en Azure para evitar consumo de créditos." -ForegroundColor Yellow
$confirm = Read-Host "¿Deseas eliminar el grupo de recursos 'rg-eventpass-prod'? (s/n)"

if ($confirm -eq "s" -or $confirm -eq "S") {
    Write-Host "Eliminando grupo de recursos en segundo plano..." -ForegroundColor Cyan
    az group delete --name rg-eventpass-prod --yes --no-wait
    Write-Host "✅ Petición de eliminación enviada. No se generarán más cobros a tu saldo de Azure for Students." -ForegroundColor Green
} else {
    Write-Host "Operación cancelada." -ForegroundColor Gray
}
