# Guía de Despliegue en Azure (Azure for Students)

Esta carpeta contiene la automatización completa para desplegar el proyecto en la nube de Microsoft Azure utilizando **Azure Container Apps (ACA)** y **Azure Database for MySQL Flexible Server**, optimizado específicamente para cuidar tu saldo de **Azure for Students** ($100 USD anuales).

---

## Arquitectura en Azure

```text
                                [ INTERNET ]
                                     │ (HTTPS Público)
                                     ▼
                            ┌─────────────────┐
                            │  frontend (ACA) │
                            │ (Nginx Gateway) │
                            └────────┬────────┘
                                     │ (Red Privada Interna de Azure)
          ┌──────────────────────────┼──────────────────────────┐
          ▼                          ▼                          ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ ms-usuarios(ACA) │       │ ms-lugares (ACA) │       │ ms-eventos (ACA) │
└─────────┬────────┘       └─────────┬────────┘       └─────────┬────────┘
          │                          │                          │
          └──────────────────────────┼──────────────────────────┘
                                     │
                                     ▼
                          ┌─────────────────────┐
                          │   ms-compras (ACA)  │
                          └──────────┬──────────┘
                                     │ (MySQL Port 3306)
                                     ▼
                    ┌─────────────────────────────────┐
                    │  Azure Database for MySQL       │
                    │   Flexible Server (B1ms Tier)   │
                    └─────────────────────────────────┘
```

### ¿Por qué esta arquitectura es ideal para estudiantes?
1. **Escalado a cero (Scale to 0):** Los microservicios entran en modo de reposo cuando no reciben peticiones, por lo que **no consumen créditos** mientras no se usen.
2. **Nivel Gratuito Mensual:** Azure Container Apps ofrece cada mes 180,000 vCPU-segundos, 360,000 GiB-segundos y 2 millones de peticiones sin costo.
3. **Seguridad y Aislamiento:** Solo el `frontend` tiene salida pública a internet con certificado SSL/HTTPS automático. Los 4 microservicios backend están en la red interna privada de Azure, inaccesibles desde internet directo.
4. **Cero problemas de CORS:** Nginx actúa como API Gateway en el frontend, enrutando `/api/usuarios`, `/api/eventos`, etc.

---

##  Despliegue Automatizado

### Paso 1: Abrir PowerShell en la raíz del proyecto
Abre una terminal de PowerShell en:
```bash
c:\Users\USUARIO\Desktop\proyecto_azure_docker
```

### Paso 2: Ejecutar el script de despliegue
```powershell
.\azure\deploy.ps1
```

### ¿Qué hace el script automáticamente?
1. Inicia sesión en tu cuenta de Azure (`az login`).
2. Crea el Grupo de Recursos (`rg-eventpass-prod`) en la región `eastus`.
3. Crea un **Azure Container Registry (ACR)** y compila las 5 imágenes Docker en la nube.
4. Crea la instancia de **Azure Database for MySQL Flexible Server** (Tier `Standard_B1ms`).
5. Configura las reglas de firewall y los esquemas de bases de datos desde `bd/scripts_bd.sql`.
6. Crea el entorno de Container Apps y levanta los 4 microservicios privados y el frontend público.
7. Te entrega la URL pública final (`https://frontend.xxx.azurecontainerapps.io`).

---

## Limpieza (Para no gastar créditos)

Cuando termines tu entrega, exposición o evaluación del proyecto, ejecuta:

```powershell
.\azure\cleanup.ps1
```
O directamente con Azure CLI:
```bash
az group delete --name rg-eventpass-prod --yes --no-wait
```
> Esto borrará el grupo de recursos completo y detendrá cualquier consumo de tu saldo de Azure for Students.
