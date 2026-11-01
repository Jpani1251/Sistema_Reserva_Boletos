# Documentación Técnica del Proyecto: Sistema de Microservicios EventPass en Docker y Microsoft Azure

---

## 1. Resumen Ejecutivo

El presente proyecto consiste en la corrección, refactorización arquitectónica, contenerización y despliegue en la nube de **EventPass**, una plataforma distribuida para la gestión de recintos, publicación de eventos y venta de boletos con selección interactiva de asientos en tiempo real.

El sistema está compuesto por **cuatro microservicios independientes** desarrollados en **Java 17 con Spring Boot 3**, **cuatro bases de datos relacionales desacopladas** en **MySQL 8.0**, y un **Frontend SPA (Single Page Application)** servido mediante **Nginx**, el cual actúa simultáneamente como servidor web y **API Gateway (Reverse Proxy)**. Toda la solución fue empaquetada en contenedores **Docker**, orquestada con **Docker Compose** y desplegada en una **Máquina Virtual Linux (Ubuntu Server)** en **Microsoft Azure**.

---

## 2. Arquitectura General del Sistema

| Componente | Tecnología | Puerto Interno | Puerto Expuesto | Base de Datos Asignada | Responsabilidad Principal |
| :--- | :--- | :---: | :---: | :--- | :--- |
| **Frontend + API Gateway** | HTML5, CSS3, JS + Nginx Alpine | `80` | `3000` | *N/A* | Interfaz de usuario SPA y enrutamiento inverso (`/api/*`) hacia los microservicios. |
| **ms-usuarios** | Java 17 / Spring Boot 3 | `8081` | `8081` | `bdgu_2022630310` | Registro de usuarios, autenticación, generación y validación de tokens de sesión (vigencia de 20 min). |
| **ms-lugares** | Java 17 / Spring Boot 3 | `8082` | `8082` | `bdgl_2022630310` | Alta, edición, borrado y consulta de recintos (dimensiones de filas $\times$ columnas y mapa de asientos). |
| **ms-eventos** | Java 17 / Spring Boot 3 | `8083` | `8083` | `bdge_2022630310` | Gestión de eventos, precios por fila y consolidación del mapa de disponibilidad de asientos. |
| **ms-compras** | Java 17 / Spring Boot 3 | `8084` | `8084` | `bdgc_2022630310` | Procesamiento de compra de boletos, registro de asientos ocupados y consulta de historial por usuario. |
| **Base de Datos** | MySQL 8.0 | `3306` | `3307` | 4 Esquemas independientes | Almacenamiento persistente bajo el patrón *Database-per-Service*. |

---

## 3. Etapas del Desarrollo (¿Qué se hizo y por qué se hizo?)

### Etapa 1: Diagnóstico y Corrección del Código Base
* **¿Qué se hizo?**
  1. Se reparó el código fuente de `ms-lugares` (`MsLugaresApplication.java`), el cual presentaba llaves desbalanceadas, fragmentos de código duplicados e incompletos en el método `actualizaLugar` y errores de sintaxis que impedían su compilación.
  2. En `ms-eventos`, se corrigió el nombre de la clase principal (que erróneamente se llamaba `MsUsuariosApplication`) a `MsEventosApplication`.
  3. Se estandarizaron los archivos `pom.xml` de los cuatro proyectos para que sus `artifactId` y nombres coincidieran con cada microservicio (`ms-usuarios`, `ms-lugares`, `ms-eventos`, `ms-compras`).
* **¿Por qué se hizo?**
  Sin estas correcciones estructurales, el compilador de Maven fallaba al intentar generar los archivos ejecutables `.jar`, haciendo imposible construir las imágenes de Docker.

---

### Etapa 2: Refactorización a Arquitectura en Capas (Layered Architecture)
* **¿Qué se hizo?**
  Originalmente, cada microservicio concentraba toda su lógica (rutas HTTP, consultas SQL crudas, validaciones y llamadas a otros servicios) dentro de una única clase monolítica. Se rediseñaron los 4 microservicios dividiendo responsabilidades en paquetes especializados:
  * **`controller/`**: Manejo de peticiones y respuestas HTTP REST (`@RestController`, `@RequestMapping`, `@CrossOrigin`).
  * **`service/`**: Reglas de negocio, validación de credenciales, cálculo de vigencia de tokens y coordinación de operaciones.
  * **`repository/`**: Capa de persistencia de datos mediante `JdbcTemplate` con consultas SQL parametrizadas.
  * **`client/`**: Clientes HTTP basados en `RestTemplate` (`UsuarioClient`, `LugaresClient`, `ComprasClient`) para la comunicación interna entre microservicios.
  * **`model/` y `dto/`**: Representación estructurada de entidades y objetos de transferencia de datos.
  * **`config/`**: Definición de Beans de infraestructura de Spring (`AppConfig`).
* **¿Por qué se hizo?**
  Para aplicar el principio de **Responsabilidad Única (SRP)** y **Separación de Intereses (SoC)**. Esto facilita la lectura, el mantenimiento, la depuración de errores y sigue el estándar profesional de desarrollo empresarial con Spring Boot.

---

### Etapa 3: Parametrización mediante Variables de Entorno
* **¿Qué se hizo?**
  Se modificaron los archivos `application.properties` y los clientes HTTP de los cuatro microservicios para reemplazar las direcciones fijas (`localhost:3307`, `localhost:8081`, etc.) por variables de entorno dinámicas con valores por defecto:
  ```properties
  spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3307}/bdgu_${BOLETA:2022630310}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  spring.datasource.username=${DB_USER:root}
  spring.datasource.password=${DB_PASS:root}
  ```
  Asimismo, las URLs entre microservicios se configuraron dinámicamente mediante `URL_USUARIOS`, `URL_LUGARES` y `URL_COMPRAS`.
* **¿Por qué se hizo?**
  En un entorno de contenedores Docker, cada contenedor tiene su propia interfaz de red aislada; por lo tanto, `localhost` apunta al contenedor mismo y no a la computadora anfitriona ni a los demás contenedores. Parametrizar las conexiones permite que el mismo código funcione sin cambios tanto en una PC local como en un servidor en Azure.

---

### Etapa 4: Implementación del Patrón *Database-per-Service* en MySQL
* **¿Qué se hizo?**
  Se estructuró el script de inicialización `bd/scripts_bd.sql` creando cuatro bases de datos independientes asociadas a la boleta `2022630310`:
  1. `bdgu_2022630310`: Tabla `usuarios` (almacena datos personales, contraseña cifrada/texto, foto en `LONGTEXT`, token y marca de tiempo para sesión de 20 minutos).
  2. `bdgl_2022630310`: Tabla `lugares` (almacena nombre del recinto, total de filas, columnas y cadena de configuración de asientos).
  3. `bdge_2022630310`: Tabla `eventos` (almacena nombre, fecha, hora, `id_lugar`, lista de precios por fila e imagen promocional en `LONGTEXT`).
  4. `bdgc_2022630310`: Tabla `compras` (almacena el vínculo entre `id_usuario`, `id_evento`, cadena de asientos comprados y fecha de transacción).
* **¿Por qué se hizo?**
  Uno de los principios fundamentales de los microservicios es el **desacoplamiento de datos**: ningún microservicio debe consultar directamente la base de datos de otro. Cuando `ms-eventos` necesita presentar el detalle de un evento con sus asientos disponibles y ocupados, consulta vía HTTP REST a `ms-lugares` y a `ms-compras`, combinando las respuestas en memoria.

---

### Etapa 5: Construcción del Frontend SPA, Compresión de Imágenes y API Gateway con Nginx
* **¿Qué se hizo?**
  1. **Interfaz SPA (`frontend/`):** Se desarrolló una aplicación web moderna y responsiva que consume todos los flujos del sistema: registro con foto de perfil, login con persistencia de sesión, administración de lugares y eventos, renderizado dinámico de la cuadrícula de asientos y compra de boletos.
  2. **Compresión automática en cliente (`HTML5 Canvas`):** En `frontend/js/app.js` se implementó una función que intercepta las imágenes subidas por el usuario, las redimensiona a un máximo de $800 \times 800\text{ px}$ y las comprime en formato JPEG al $70\%$ antes de codificarlas en Base64.
  3. **API Gateway con Nginx (`frontend/nginx.conf`):** Se configuró Nginx para servir la aplicación en `/` y actuar como proxy inverso en las rutas `/api/usuarios/`, `/api/lugares/`, `/api/eventos/` y `/api/compras/`, redirigiéndolas internamente hacia `http://ms-usuarios:8081`, `http://ms-lugares:8082`, etc.
* **¿Por qué se hizo?**
  * **El API Gateway en Nginx** resuelve tres problemas críticos al desplegar en la nube: evita bloqueos de seguridad **CORS** en el navegador, evita tener que cambiar IPs fijas en el código JavaScript cuando cambia la IP pública de la máquina virtual, y ofrece un único punto de entrada seguro (`:3000`).
  * **La compresión de imágenes** evita que fotografías de alta resolución generen cadenas Base64 de más de $20\text{ MB}$ que provocan errores de límite de memoria en Spring Boot (`StreamConstraintsException`) o rechazos en el servidor web (`413 Content Too Large`).

---

### Etapa 6: Contenerización Multi-Stage (Docker) y Orquestación (Docker Compose)
* **¿Qué se hizo?**
  1. **Dockerfiles Multi-Stage:** En cada microservicio se diseñó un `Dockerfile` de dos etapas:
     * **Etapa 1 (`build`):** Utiliza la imagen `maven:3.9.6-eclipse-temurin-17` para descargar las librerías y compilar el código fuente en un archivo `.jar`.
     * **Etapa 2 (`runtime`):** Utiliza una imagen ligera `eclipse-temurin:17-jre-alpine`, copiando únicamente el `.jar` resultante de la etapa anterior.
  2. **Orquestación (`docker-compose.yml`):** Se definieron los 5 servicios (`ms-usuarios`, `ms-lugares`, `ms-compras`, `ms-eventos`, `frontend`) conectados a través de una red privada virtual (`app-network`) y enlazados al motor MySQL mediante `host.docker.internal:3307`.
* **¿Por qué se hizo?**
  * El uso de **Multi-Stage Builds** permite que cualquier servidor pueda compilar el proyecto sin necesidad de instalar Java o Maven manualmente, además de reducir el tamaño final de cada contenedor de ~$800\text{ MB}$ a ~$90\text{ MB}$, ahorrando memoria RAM y almacenamiento en la máquina virtual de Azure.
  * **Docker Compose** garantiza que todos los servicios se levanten en el orden correcto, con sus variables de entorno y su red interna configurada automáticamente.

---

### Etapa 7: Despliegue en Microsoft Azure (Máquina Virtual Ubuntu)
* **¿Qué se hizo?**
  1. **Creación de la Máquina Virtual:** En el Portal de Azure (bajo la suscripción *Azure for Students*), se aprovisionó una máquina virtual **Ubuntu Server LTS (x64 Gen2)** con arquitectura de $2\text{ vCPUs}$ y $4\text{ GiB}$ de RAM (`Standard_B2s` / `B2ats_v2`).
  2. **Configuración del Firewall de Azure (Network Security Group - NSG):** En el apartado de *Configuración de red* de la VM se crearon reglas de entrada TCP para permitir el tráfico en los puertos:
     * `22` (Conexión remota por SSH).
     * `80` y `3000` (Acceso público al Frontend web y API Gateway).
     * `8081-8084` (Puertos de los microservicios).
     * `3307` (Conexión remota al contenedor de MySQL para visualización desde clientes externos como DBeaver).
  3. **Transferencia de Archivos e Instalación de Docker:** Desde PowerShell en Windows se transfirió el proyecto hacia la VM usando `scp -r`. Posteriormente, por SSH se instalaron los paquetes `docker.io` y `docker-compose-v2`, agregando el usuario al grupo `docker`.
  4. **Despliegue de Base de Datos y Microservicios:**
     * Se levantó el contenedor `mysql:8.0` mapeado al puerto `3307` y se inyectó el esquema `bd/scripts_bd.sql`.
     * Se corrigieron los permisos de lectura del sistema de archivos en Linux (`chmod -R 755 ~/proyecto_azure_docker`) para evitar el error `403 Forbidden` en Nginx ocasionado por los permisos restrictivos con los que `scp` transfiere archivos desde Windows.
     * Se ejecutó `docker compose up -d --build` para compilar e iniciar todos los servicios en segundo plano.
* **¿Por qué se hizo así?**
  * Las suscripciones estudiantiles (*Azure for Students*) cuentan con políticas que restringen ciertas regiones y bloquean tareas de compilación en servicios administrados como ACR Tasks (`TasksOperationsNotAllowed`).
  * El despliegue sobre una **Máquina Virtual IaaS con Docker Compose** elimina por completo dichas restricciones, mantiene el $100\%$ de compatibilidad con el entorno de desarrollo local, evita tiempos de espera por arranque en frío (*cold starts*) y permite administrar tanto los contenedores como la base de datos de forma transparente y con bajo consumo de créditos.

---

## 4. Comandos de Referencia y Mantenimiento en la Máquina Virtual

| Acción | Comando en la Máquina Virtual (SSH) |
| :--- | :--- |
| **Ver estado de todos los contenedores** | `docker ps` |
| **Reconstruir y actualizar cambios del proyecto** | `cd ~/proyecto_azure_docker && docker compose up -d --build` |
| **Corregir permisos si se suben archivos nuevos** | `chmod -R 755 ~/proyecto_azure_docker && docker restart frontend-app` |
| **Ver logs de un microservicio en tiempo real** | `docker logs -f ms-eventos` |
| **Consultar las bases de datos en MySQL** | `docker exec -it mysql-eventpass mysql -uroot -proot -e "SHOW DATABASES;"` |
| **Detener los microservicios** | `cd ~/proyecto_azure_docker && docker compose down` |
