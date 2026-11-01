# ==============================================================================
# SCRIPT DE DESPLIEGUE EN AZURE CONTAINER APPS
# Proyecto: Sistema de Microservicios EventPass
# Diseñado para suscripción Azure for Students (Optimizado en costos y nivel gratuito)
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ">>> INICIANDO DESPLIEGUE EN AZURE CONTAINER APPS (STUDENTS)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. VERIFICAR INICIO DE SESIÓN EN AZURE
Write-Host "`n[1/7] Verificando sesion en Azure CLI..." -ForegroundColor Yellow
$ErrorActionPreference = "Continue"
$accountName = az account show --query "name" -o tsv 2>$null

if (-not $accountName -or $LASTEXITCODE -ne 0) {
    Write-Host "La consola de Azure CLI aun no esta conectada." -ForegroundColor Cyan
    Write-Host "Abriendo tu navegador para vincular tu cuenta de estudiante..." -ForegroundColor Yellow
    az login
}

$ErrorActionPreference = "Continue"
$subInfo = az account show --query "{Nombre:name, Id:id}" -o tsv
Write-Host "Conectado a la suscripcion: $subInfo" -ForegroundColor Green

# Registrar proveedores requeridos en la suscripcion
az provider register --namespace Microsoft.App --output none 2>$null
az provider register --namespace Microsoft.OperationalInsights --output none 2>$null
az provider register --namespace Microsoft.ContainerRegistry --output none 2>$null
az provider register --namespace Microsoft.DBforMySQL --output none 2>$null

# 2. DEFINICIÓN DE VARIABLES
$LOCATION = "mexicocentral"
$RG = "rg-eventpass-prod"
$ACR_NAME = "acreventpass" + (Get-Random -Minimum 1000 -Maximum 9999)
$ENV_NAME = "cae-eventpass"
$MYSQL_NAME = "mysql-eventpass-" + (Get-Random -Minimum 1000 -Maximum 9999)
$MYSQL_USER = "adminuser"
$MYSQL_PASS = "EventPass2026!Azure"
$BOLETA = "2022630310"

Write-Host "`nConfiguracion del despliegue:" -ForegroundColor Cyan
Write-Host " - Resource Group: $RG"
Write-Host " - Region permitida: $LOCATION (Mexico Central)"
Write-Host " - Registro ACR: $ACR_NAME"
Write-Host " - Servidor MySQL: $MYSQL_NAME"
Write-Host " - Entorno Container Apps: $ENV_NAME"

# 3. CREAR GRUPO DE RECURSOS
Write-Host "`n[2/7] Creando Grupo de Recursos ($RG)..." -ForegroundColor Yellow
az group create --name $RG --location $LOCATION --output none
Write-Host "Grupo de recursos listo." -ForegroundColor Green

# 4. CREAR AZURE CONTAINER REGISTRY (SKU Basic para estudiantes)
Write-Host "`n[3/7] Creando Azure Container Registry ($ACR_NAME)..." -ForegroundColor Yellow
az acr create --resource-group $RG --name $ACR_NAME --sku Basic --admin-enabled true --output none

$ACR_SERVER = az acr show --name $ACR_NAME --query loginServer -o tsv 2>$null
$ACR_PWD = az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv 2>$null

if (-not $ACR_SERVER) {
    Write-Host "Error: No se pudo crear o consultar el registro ACR." -ForegroundColor Red
    exit 1
}
Write-Host "ACR creado exitosamente: $ACR_SERVER" -ForegroundColor Green

# 5. CONSTRUIR Y SUBIR IMÁGENES A ACR
Write-Host "`n[4/7] Compilando y subiendo imágenes a ACR..." -ForegroundColor Yellow
$services = @("ms-usuarios", "ms-lugares", "ms-eventos", "ms-compras", "frontend")
foreach ($svc in $services) {
    Write-Host "  -> Construyendo y subiendo $svc..." -ForegroundColor Cyan
    az acr build --registry $ACR_NAME --image "${svc}:latest" "./$svc" --output none
    Write-Host "     $svc subido a ACR." -ForegroundColor Green
}

# 6. CREAR BASE DE DATOS AZURE MYSQL FLEXIBLE SERVER
Write-Host "`n[5/7] Creando Azure Database for MySQL Flexible Server ($MYSQL_NAME)..." -ForegroundColor Yellow
Write-Host "  (Tier Burstable B1ms: bajo consumo para estudiantes)" -ForegroundColor DarkGray
az mysql flexible-server create `
    --resource-group $RG `
    --name $MYSQL_NAME `
    --location $LOCATION `
    --admin-user $MYSQL_USER `
    --admin-password $MYSQL_PASS `
    --sku-name Standard_B1ms `
    --tier Burstable `
    --storage-size 20 `
    --version 8.0.21 `
    --public-access 0.0.0.0 `
    --output none

$MYSQL_HOST = az mysql flexible-server show --resource-group $RG --name $MYSQL_NAME --query fullyQualifiedDomainName -o tsv 2>$null

if (-not $MYSQL_HOST) {
    Write-Host "Error: No se pudo obtener el host del servidor MySQL." -ForegroundColor Red
    exit 1
}
Write-Host "MySQL listo en: $MYSQL_HOST" -ForegroundColor Green

# Habilitar regla de firewall para permitir conexiones desde servicios de Azure
az mysql flexible-server firewall-rule create `
    --resource-group $RG `
    --name $MYSQL_NAME `
    --rule-name AllowAllAzureIPs `
    --start-ip-address 0.0.0.0 `
    --end-ip-address 0.0.0.0 `
    --output none

# Inicializar bases de datos y tablas desde scripts_bd.sql
Write-Host "  -> Inicializando esquemas de BD desde bd/scripts_bd.sql..." -ForegroundColor Cyan
if (Get-Command mysql -ErrorAction SilentlyContinue) {
    cmd.exe /c "mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASS < ./bd/scripts_bd.sql"
    Write-Host "Tablas inicializadas con cliente local mysql." -ForegroundColor Green
} elseif (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "  -> Importando tablas usando Docker..." -ForegroundColor DarkGray
    docker run --rm -v "${PWD}/bd:/bd" -e "MYSQL_PWD=$MYSQL_PASS" mysql:8.0 sh -c "mysql -h $MYSQL_HOST -u $MYSQL_USER < /bd/scripts_bd.sql"
    Write-Host "Tablas inicializadas con contenedor Docker." -ForegroundColor Green
} else {
    Write-Host "Nota: No se detectó cliente mysql local. Se importará mediante un contenedor temporal en Azure..." -ForegroundColor DarkGray
    az container create `
        --resource-group $RG `
        --name init-db-temp `
        --image mysql:8.0 `
        --restart-policy Never `
        --environment-variables MYSQL_PWD=$MYSQL_PASS `
        --command-line "sh -c 'echo \"CREATE DATABASE IF NOT EXISTS bdgu_$BOLETA; CREATE DATABASE IF NOT EXISTS bdgl_$BOLETA; CREATE DATABASE IF NOT EXISTS bdge_$BOLETA; CREATE DATABASE IF NOT EXISTS bdgc_$BOLETA;\" | mysql -h $MYSQL_HOST -u $MYSQL_USER'" `
        --output none
}

# 7. CREAR ENTORNO DE AZURE CONTAINER APPS
Write-Host "`n[6/7] Creando Entorno de Container Apps ($ENV_NAME)..." -ForegroundColor Yellow
az containerapp env create `
    --name $ENV_NAME `
    --resource-group $RG `
    --location $LOCATION `
    --output none
Write-Host "Entorno Container Apps listo." -ForegroundColor Green

# 8. DESPLEGAR MICROSERVICIOS (Ingress interno)
Write-Host "`n[7/7] Desplegando Contenedores en Azure..." -ForegroundColor Yellow

# ms-usuarios
Write-Host "  -> Desplegando ms-usuarios..." -ForegroundColor Cyan
az containerapp create `
    --name ms-usuarios `
    --resource-group $RG `
    --environment $ENV_NAME `
    --image "$ACR_SERVER/ms-usuarios:latest" `
    --registry-server $ACR_SERVER `
    --registry-username $ACR_NAME `
    --registry-password $ACR_PWD `
    --target-port 8081 `
    --ingress internal `
    --cpu 0.25 --memory 0.5Gi `
    --min-replicas 0 --max-replicas 1 `
    --env-vars DB_HOST=$MYSQL_HOST DB_PORT="3306" DB_USER=$MYSQL_USER DB_PASS=$MYSQL_PASS BOLETA=$BOLETA `
    --output none

# ms-lugares
Write-Host "  -> Desplegando ms-lugares..." -ForegroundColor Cyan
az containerapp create `
    --name ms-lugares `
    --resource-group $RG `
    --environment $ENV_NAME `
    --image "$ACR_SERVER/ms-lugares:latest" `
    --registry-server $ACR_SERVER `
    --registry-username $ACR_NAME `
    --registry-password $ACR_PWD `
    --target-port 8082 `
    --ingress internal `
    --cpu 0.25 --memory 0.5Gi `
    --min-replicas 0 --max-replicas 1 `
    --env-vars DB_HOST=$MYSQL_HOST DB_PORT="3306" DB_USER=$MYSQL_USER DB_PASS=$MYSQL_PASS BOLETA=$BOLETA URL_USUARIOS="http://ms-usuarios:8081" `
    --output none

# ms-compras
Write-Host "  -> Desplegando ms-compras..." -ForegroundColor Cyan
az containerapp create `
    --name ms-compras `
    --resource-group $RG `
    --environment $ENV_NAME `
    --image "$ACR_SERVER/ms-compras:latest" `
    --registry-server $ACR_SERVER `
    --registry-username $ACR_NAME `
    --registry-password $ACR_PWD `
    --target-port 8084 `
    --ingress internal `
    --cpu 0.25 --memory 0.5Gi `
    --min-replicas 0 --max-replicas 1 `
    --env-vars DB_HOST=$MYSQL_HOST DB_PORT="3306" DB_USER=$MYSQL_USER DB_PASS=$MYSQL_PASS BOLETA=$BOLETA URL_USUARIOS="http://ms-usuarios:8081" `
    --output none

# ms-eventos
Write-Host "  -> Desplegando ms-eventos..." -ForegroundColor Cyan
az containerapp create `
    --name ms-eventos `
    --resource-group $RG `
    --environment $ENV_NAME `
    --image "$ACR_SERVER/ms-eventos:latest" `
    --registry-server $ACR_SERVER `
    --registry-username $ACR_NAME `
    --registry-password $ACR_PWD `
    --target-port 8083 `
    --ingress internal `
    --cpu 0.25 --memory 0.5Gi `
    --min-replicas 0 --max-replicas 1 `
    --env-vars DB_HOST=$MYSQL_HOST DB_PORT="3306" DB_USER=$MYSQL_USER DB_PASS=$MYSQL_PASS BOLETA=$BOLETA URL_USUARIOS="http://ms-usuarios:8081" URL_LUGARES="http://ms-lugares:8082" URL_COMPRAS="http://ms-compras:8084" `
    --output none

# frontend (Ingress externo publico con HTTPS)
Write-Host "  -> Desplegando frontend (Ingress publico HTTPS)..." -ForegroundColor Cyan
az containerapp create `
    --name frontend `
    --resource-group $RG `
    --environment $ENV_NAME `
    --image "$ACR_SERVER/frontend:latest" `
    --registry-server $ACR_SERVER `
    --registry-username $ACR_NAME `
    --registry-password $ACR_PWD `
    --target-port 80 `
    --ingress external `
    --cpu 0.25 --memory 0.5Gi `
    --min-replicas 1 --max-replicas 1 `
    --output none

$FRONTEND_URL = az containerapp show --name frontend --resource-group $RG --query "properties.configuration.ingress.fqdn" -o tsv

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host ">>> DESPLIEGUE COMPLETADO CON EXITO!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "`nTu aplicacion esta publicada en internet en:" -ForegroundColor Cyan
Write-Host "URL: https://$FRONTEND_URL" -ForegroundColor Yellow -BackgroundColor Black
Write-Host "`nPara eliminar los recursos cuando termines de evaluar y no gastar creditos:" -ForegroundColor Gray
Write-Host "   az group delete --name $RG --yes --no-wait" -ForegroundColor Gray
