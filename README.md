# Sistema de Facturación SOA

Sistema de ventas y facturación empresarial desarrollado con Spring Boot 3.x y Java 21.

## Requisitos previos

- Java 21 (JDK)
- Maven 3.9+
- XAMPP con MySQL activo en puerto 3306
- Git

## Configuración de base de datos

1. Inicia XAMPP y activa el módulo **MySQL**
2. Abre **phpMyAdmin** o MySQL Workbench
3. Ejecuta el script SQL:
   ```
   src/main/resources/db/schema.sql
   ```
   Esto crea la base de datos `facturacion_db` con todas las tablas y datos iniciales.

## Ejecución del proyecto

```bash
# Clonar repositorio
git clone <url-repositorio>
cd FacturacionSOA

# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run
```

La aplicación inicia en: http://localhost:8080

## Documentación API (Swagger)

Accede a: http://localhost:8080/swagger-ui.html

## Autenticación

El sistema usa JWT. Para obtener un token:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "tu_password"
}
```

Incluir el token en las peticiones:
```
Authorization: Bearer <token>
```

## Roles y permisos

| Rol       | Permisos                              |
|-----------|---------------------------------------|
| ADMIN     | Acceso completo a todos los módulos   |
| CAJERO    | Ventas, facturas y clientes           |
| BODEGUERO | Inventario y productos                |

## Estructura de endpoints

| Módulo      | Endpoints                              |
|-------------|----------------------------------------|
| Auth        | POST /api/auth/login                   |
| Usuarios    | GET/POST/PUT/DELETE /api/usuarios      |
| Clientes    | GET/POST/PUT/DELETE /api/clientes      |
| Categorías  | GET/POST/PUT/DELETE /api/categorias    |
| Productos   | GET/POST/PUT/DELETE /api/productos     |
| Inventario  | GET/PUT /api/inventario                |
| Ventas      | GET/POST /api/ventas                   |
| Facturas    | GET/POST /api/facturas                 |

## Sucursales configuradas

- **Sucursal Quito** — Av. Amazonas N37-29
- **Sucursal Ambato** — Calle Bolívar 12-34
- **Sucursal Cuenca** — Av. Solano 4-50

## Tecnologías

- Spring Boot 3.2.5
- Java 21
- MySQL 8.x (XAMPP)
- Spring Security + JWT (jjwt 0.12.3)
- Spring Data JPA + Hibernate
- Springdoc OpenAPI 2.3.0 (Swagger)
- Lombok
- iText 5.5.13.3 (PDF)
- Thymeleaf
