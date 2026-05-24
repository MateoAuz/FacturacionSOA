# Sistema de Facturación SOA

Sistema de ventas y facturación empresarial — Spring Boot 3.2.5 + Java 21 + MySQL.

**Repositorio:** https://github.com/MateoAuz/FacturacionSOA

---

## Requisitos previos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| JDK | 21 o superior | **No sirve Java 8 ni Java 11.** Ver instalación abajo. |
| XAMPP | Cualquiera reciente | Solo se necesita el módulo **MySQL** |
| Git | Cualquiera | Para clonar el repositorio |
| Maven | No requerido | El proyecto incluye `mvnw` que lo descarga automáticamente |

> **Maven NO necesita instalarse.** El proyecto incluye el Maven Wrapper (`mvnw.cmd` en Windows,
> `./mvnw` en Mac/Linux). El primer `mvnw` descarga Maven 3.9 automáticamente.

---

## Paso 1 — Verificar / instalar JDK 21+

Abre una terminal y ejecuta:

```
java -version
```

Si el resultado muestra **java version "21"** o superior, salta al Paso 2.

Si muestra una versión menor (ej. 1.8, 11, 17), necesitas instalar JDK 21+:

- Descarga desde: https://www.oracle.com/java/technologies/downloads/#java21
- Instala y anota la ruta de instalación (ej. `C:\Program Files\Java\jdk-21`)

### Configurar JAVA_HOME (Windows)

Es probable que tu sistema tenga Java 8 en el PATH pero JDK 21 instalado aparte.
Debes indicarle a Maven cuál usar:

**Opción A — Solo para la sesión actual de terminal (más fácil):**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"   # ajusta la ruta a tu instalación
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version   # debe mostrar 21
```

**Opción B — Permanente (recomendado para el equipo):**
1. Busca "Variables de entorno" en el menú inicio
2. En "Variables del sistema" → `JAVA_HOME` → cambia el valor a la ruta de tu JDK 21+
3. En la variable `Path` → mueve la entrada de JDK 21 al inicio de la lista
4. Cierra y vuelve a abrir la terminal

---

## Paso 2 — Clonar el repositorio

```bash
git clone https://github.com/MateoAuz/FacturacionSOA.git
cd FacturacionSOA
```

---

## Paso 3 — Configurar la base de datos

1. Abre **XAMPP Control Panel** y pulsa **Start** en el módulo **MySQL**
2. Abre **phpMyAdmin** en el navegador: http://localhost/phpmyadmin
3. Ve a la pestaña **SQL** (o usa "Importar")
4. Ejecuta el contenido del archivo:
   ```
   src/main/resources/db/schema.sql
   ```
   Esto crea la base de datos `facturacion_db` con todas las tablas y datos iniciales
   (roles, sucursales, categorías, IVA al 15%).

**Si tu MySQL tiene contraseña** (no es el caso por defecto en XAMPP), edita:
```
src/main/resources/application.properties
```
y cambia la línea:
```
spring.datasource.password=
```
por tu contraseña real.

---

## Paso 4 — Compilar y ejecutar

### Windows (PowerShell o CMD)

```powershell
# Si necesitas apuntar a JDK 21+ (solo si java -version no muestra 21+)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Compilar (primera vez descarga ~300 MB de dependencias, necesita internet)
.\mvnw clean compile

# Ejecutar
.\mvnw spring-boot:run
```

### Mac / Linux

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

La aplicación inicia en: **http://localhost:8080**

> **Primera vez:** la compilación descarga todas las dependencias Maven (~300 MB).
> Asegúrate de tener conexión a internet y al menos **500 MB libres en disco.**

---

## Paso 5 — Primer login

Al arrancar por primera vez, el sistema **crea automáticamente** un usuario administrador.
Verás este mensaje en la consola:

```
  Usuario admin creado:
  Username : admin
  Password : Admin1234
  CAMBIA LA CONTRASEÑA despues del primer login.
```

Prueba el login con Swagger o con cualquier cliente REST (Postman, Insomnia):

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin1234"
}
```

Respuesta esperada:
```json
{
  "exitoso": true,
  "datos": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tipo": "Bearer",
    "username": "admin",
    "rol": "ADMIN"
  }
}
```

Copia el token y úsalo en las siguientes peticiones:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Swagger UI (documentación interactiva)

Accede a: **http://localhost:8080/swagger-ui.html**

Desde ahí puedes probar todos los endpoints sin Postman:
1. Haz login en `POST /api/auth/login`
2. Copia el token de la respuesta
3. Pulsa el botón **Authorize** (candado) en la esquina superior derecha
4. Ingresa `Bearer <tu-token>` y confirma
5. Ya puedes ejecutar cualquier endpoint autenticado

---

## Configuración de IDE

### IntelliJ IDEA (recomendado)

1. `File → Open` → selecciona la carpeta del proyecto
2. IntelliJ detecta el `pom.xml` automáticamente
3. Espera a que descargue las dependencias (barra de progreso abajo)
4. Para ejecutar: abre `FacturacionSoaApplication.java` → botón ▶ verde
5. **Importante:** en `File → Project Structure → Project SDK` asegúrate de tener JDK 21+

### VS Code

1. Instala la extensión **Extension Pack for Java**
2. Abre la carpeta del proyecto
3. VS Code detecta el proyecto Maven automáticamente

---

## Problemas comunes

### "Java 8 found, needs 21"
Maven Wrapper está usando el Java incorrecto.
Solución: configura `JAVA_HOME` como se describe en el Paso 1.

### "Communications link failure" al arrancar
MySQL no está corriendo.
Solución: abre XAMPP y asegúrate de que el módulo MySQL esté en verde (Started).

### "Unknown database 'facturacion_db'"
El script SQL no fue ejecutado.
Solución: ejecuta `src/main/resources/db/schema.sql` en phpMyAdmin (Paso 3).

### "Access denied for user 'root'"
Tu MySQL tiene contraseña configurada.
Solución: edita `spring.datasource.password=` en `application.properties`.

### Error al compilar: permisos en mvnw (Mac/Linux)
```bash
chmod +x mvnw
./mvnw clean compile
```

### Primera compilación muy lenta
Normal. Está descargando ~300 MB de dependencias Maven. Solo ocurre la primera vez.

---

## Estructura del proyecto

```
src/main/java/com/empresa/sistema/
├── controller/          # Endpoints REST (8 controllers)
├── service/             # Interfaces de servicio
│   └── impl/            # Implementaciones de los servicios
├── repository/          # Interfaces JPA (acceso a BD)
├── entity/              # Clases JPA que mapean las tablas
├── dto/
│   ├── request/         # Objetos de entrada de los endpoints
│   └── response/        # Objetos de respuesta de los endpoints
├── security/
│   ├── jwt/             # Filtro y utilidad JWT
│   └── config/          # Configuración de Spring Security
├── config/              # OpenAPI / Swagger
└── util/                # Constantes, manejador de excepciones, inicializador
src/main/resources/
├── application.properties       # Configuración de la app
└── db/schema.sql                # Script de base de datos
```

---

## Endpoints disponibles

| Módulo | Método | Ruta | Rol requerido |
|---|---|---|---|
| Auth | POST | `/api/auth/login` | Público |
| Usuarios | GET/POST/PUT/DELETE | `/api/usuarios` | ADMIN |
| Clientes | GET/POST/PUT/DELETE | `/api/clientes` | ADMIN, CAJERO |
| Categorías | GET/POST/PUT/DELETE | `/api/categorias` | Autenticado |
| Productos | GET/POST/PUT/DELETE | `/api/productos` | Autenticado |
| Inventario | GET/PUT/PATCH | `/api/inventario` | ADMIN, BODEGUERO |
| Ventas | GET/POST/PATCH | `/api/ventas` | ADMIN, CAJERO |
| Facturas | GET/POST/PATCH | `/api/facturas` | ADMIN, CAJERO |

---

## Roles y permisos

| Rol | Acceso |
|---|---|
| **ADMIN** | Todo el sistema |
| **CAJERO** | Clientes, Ventas, Facturas |
| **BODEGUERO** | Productos, Inventario |

---

## Sucursales y datos iniciales

El script SQL carga automáticamente:
- 3 sucursales: **Quito**, **Ambato**, **Cuenca**
- 3 roles: ADMIN, CAJERO, BODEGUERO
- 5 categorías de productos
- IVA al 15% (vigente desde 2024-04-01)
- Configuración de empresa de ejemplo

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Spring Boot | 3.2.5 |
| Java | 21+ |
| MySQL | 8.x (XAMPP) |
| Spring Security + JWT | jjwt 0.12.3 |
| Spring Data JPA + Hibernate | incluido en Boot |
| Springdoc OpenAPI (Swagger) | 2.3.0 |
| Lombok | 1.18.32 |
| iText PDF | 5.5.13.3 |
| Thymeleaf | incluido en Boot |
| Maven Wrapper | 3.9.15 |
