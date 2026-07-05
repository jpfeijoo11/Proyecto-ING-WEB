# CustomGate 360

CustomGate 360 es una plataforma full-stack para el monitoreo aduanero, el control de operaciones y la administración de usuarios con validaciones reforzadas de identidad y ubicación.

## ✨ Objetivo del proyecto

La aplicación permite:

- Autenticar usuarios y proteger rutas privadas mediante un guard de Angular.
- Registrar administradores con validación de cédula ecuatoriana, nombre completo y selección de ubicación.
- Gestionar operaciones aduaneras, seguimiento, estado y alertas de riesgo.
- Consultar catálogos de países con riesgo, importadores y restricciones arancelarias.
- Visualizar estadísticas de operaciones y comportamiento del sistema.

## 🧩 Arquitectura

### Backend
- Java 25
- Spring Boot 4.0.5
- Spring MVC
- Spring Data JPA
- Maven
- PostgreSQL configurado en `application.properties`
- H2 disponible para entornos de desarrollo/pruebas

### Frontend
- Angular 20
- TypeScript
- RxJS
- Angular Router
- Validaciones en formularios y guard de rutas

## 📁 Estructura del repositorio

- `backend/` — API REST con Spring Boot
  - `src/main/java/com/proyecto/backend/controllers/`
  - `src/main/java/com/proyecto/backend/services/`
  - `src/main/java/com/proyecto/backend/repositories/`
  - `src/main/java/com/proyecto/backend/models/`
  - `src/main/java/com/proyecto/backend/dto/`
- `ecoprice-front/` — aplicación Angular
  - `src/app/`
  - `src/app/components/`
  - `src/app/services/`
- `README.md` — documentación del proyecto
- `DocumentacionCore_CustomGate360.pdf` — documentación adjunta del core del sistema

## 🚀 Funcionalidades principales

### Autenticación y seguridad
- Login con credenciales de usuario.
- Validación de sesión en `localStorage`.
- Protección de rutas para `/dashboard`, `/registro` y `/operaciones` mediante `authGuard`.

### Administración de usuarios
- Registro de administradores.
- Validación de cédula ecuatoriana de 10 dígitos.
- Verificación de unicidad de `username` y `cedula`.
- Asignación de roles (por defecto `ADMIN`).

### Gestión de ubicaciones
- Obtención de países.
- Carga dinámica de provincias por país.
- Carga dinámica de ciudades por provincia.

### Operaciones aduaneras
- Alta, consulta, actualización de estado y eliminación de operaciones.
- Búsqueda por número de tracking.
- Análisis individual de operaciones.
- Consulta de alertas rojas.

### Catálogos y estadísticas
- Catálogo de países en riesgo.
- Catálogo de importadores.
- Catálogo de restricciones arancelarias.
- Endpoints de estadísticas para monitoreo.

## 🔗 Endpoints principales

### Backend
- `GET /api/status` — estado de la API
- `POST /api/admin/login` — inicio de sesión
- `POST /api/admin/registro` — registro de administrador
- `GET /api/admin/listar` — listado de usuarios
- `DELETE /api/admin/{id}` — eliminación de usuario
- `GET /api/ubicaciones/paises` — países
- `GET /api/ubicaciones/provincias/{paisId}` — provincias
- `GET /api/ubicaciones/ciudades/{provinciaId}` — ciudades
- `GET /api/operaciones` — listado de operaciones
- `POST /api/operaciones` — creación de operación
- `GET /api/operaciones/tracking/{numeroTracking}` — búsqueda por tracking
- `PUT /api/operaciones/{id}/estado` — cambio de estado
- `GET /api/operaciones/{id}/analisis` — análisis de operación
- `GET /api/operaciones/alerta-roja` — alertas críticas
- `DELETE /api/operaciones/{id}` — eliminación de operación
- `GET /api/catalogos/paises-riesgo` — países con riesgo
- `POST /api/catalogos/paises-riesgo` — alta de país con riesgo
- `GET /api/catalogos/importadores` — importadores
- `POST /api/catalogos/importadores` — alta de importador
- `GET /api/catalogos/arancelarios` — restricciones arancelarias
- `POST /api/catalogos/arancelarios` — alta de restricción
- `GET /api/estadisticas` — estadísticas del sistema

## 🖥️ Rutas del frontend

- `/login` — acceso al sistema
- `/dashboard` — panel principal
- `/registro` — administración de usuarios y altas seguras
- `/operaciones` — gestión de operaciones aduaneras

## ⚙️ Configuración local

### Prerrequisitos
- Java 25
- Maven o Maven Wrapper
- Node.js 18+
- npm
- PostgreSQL ejecutándose en `localhost:5431`

### Base de datos

Crea la base `aduanas_db` y ajusta credenciales en `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5431/aduanas_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
```

### Ejecutar backend

```bash
cd backend
./mvnw spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.

### Ejecutar frontend

```bash
cd ecoprice-front
npm install
npm start
```

La interfaz estará disponible en `http://localhost:4200`.

## 🧪 Comandos útiles

### Backend
```bash
cd backend
./mvnw test
./mvnw clean package
```

### Frontend
```bash
cd ecoprice-front
npm run build
npm test
```

## 📝 Consideraciones importantes

- El proyecto ya incluye rutas privadas protegidas por `authGuard`.
- El backend usa JPA y validaciones de dominio para controlar operaciones sensibles.
- La documentación adjunta en `DocumentacionCore_CustomGate360.pdf` puede usarse como referencia técnica y funcional.
- Si usas una base diferente a PostgreSQL, actualiza el datasource y el dialecto de Hibernate.

## 👤 Flujo recomendado de uso

1. Levantar PostgreSQL y crear la base `aduanas_db`.
2. Ejecutar el backend.
3. Ejecutar el frontend.
4. Iniciar sesión y acceder a las rutas protegidas.
5. Gestionar usuarios, ubicaciones y operaciones desde el panel.

## 📌 Estado del repositorio

Este README fue actualizado para reflejar el estado real del proyecto, los endpoints disponibles, las rutas del frontend y el proceso local de ejecución.
