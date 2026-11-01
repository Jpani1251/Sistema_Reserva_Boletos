# Sistema de Microservicios - Venta de Boletos para Eventos

Sistema distribuido basado en Spring Boot 3 y Docker, preparado para despliegue en contenedores y Azure.

---

## Arquitectura de Microservicios

| Componente | Puerto Host | Base de Datos | Responsabilidad |
|---|---|---|---|
| **frontend** | `3000` | N/A (Nginx SPA) | Interfaz web interactiva para Clientes y Administradores. |
| **ms-usuarios** | `8081` | `bdgu_2022630310` | Registro, autenticación (`/login`) y verificación de tokens. |
| **ms-lugares** | `8082` | `bdgl_2022630310` | Catálogo y alta de recintos/lugares. |
| **ms-eventos** | `8083` | `bdge_2022630310` | Consulta de eventos, imágenes y orquestación con compras. |
| **ms-compras** | `8084` | `bdgc_2022630310` | Gestión de boletos disponibles y carrito de compra. |

---

## Despliegue con Docker

### Requisitos previos
* Docker Desktop instalado y en ejecución.
* Contenedor de base de datos MySQL activo en el puerto `3307` (`mysql-local`).

### 1. Iniciar los 4 microservicios
Desde la raíz del proyecto, ejecuta:
```bash
docker compose up --build
```
> Esto compilará las imágenes de cada microservicio en una imagen ligera con Java 17 y los conectará a la red interna `app-network` y a `mysql-local` mediante `host.docker.internal:3307`.

Para ejecutarlos en segundo plano (detached):
```bash
docker compose up -d --build
```

### 2. Detener los contenedores
```bash
docker compose down
```

### 3. Ver logs en tiempo real
```bash
docker compose logs -f [ms-usuarios | ms-lugares | ms-eventos | ms-compras]
```

---

## Endpoints Principales

### `ms-usuarios` (`:8081`)
* `POST /usuarios/alta_usuario`
* `GET /usuarios/login`
* `GET /usuarios/verifica_acceso`

### `ms-lugares` (`:8082`)
* `POST /lugares/alta_lugar`
* `GET /lugares/obtiene_lugares`

### `ms-eventos` (`:8083`)
* `POST /eventos/alta_evento`
* `GET /eventos/consulta_eventos`

### `ms-compras` (`:8084`)
* `POST /compras/alta_boletos`
* `PUT /compras/compra_boletos`
* `DELETE /compras/elimina_evento_carrito_compra`
* `DELETE /compras/elimina_carrito_compra`
* `GET /compras/obtiene_carrito`
