
# Proyecto Ktor Sample

Este proyecto es un ejemplo de una API REST implementada en Kotlin utilizando Ktor, Exposed y MariaDB, siguiendo principios de Clean Architecture. La aplicación permite la gestión de usuarios y de ítems (cards) y ahora incorpora autenticación basada en JWT con gestión de sesiones. Cada vez que un usuario inicia sesión se genera un token único (JWT) que se almacena en una tabla de sesiones; este token se verifica en cada endpoint protegido y se invalida en caso de logout o cuando se produce un nuevo login.

---

## Características

### Gestión de Usuarios
- **Registro e inicio de sesión:**  
  Permite registrar e iniciar sesión usando correo electrónico y contraseña.
- **Actualización y eliminación de usuarios:**  
  Se pueden listar, actualizar y eliminar usuarios.
- **Cifrado y autenticación:**  
  Se utiliza BCrypt para cifrar contraseñas y JWT para la autenticación.  
  Cada login genera un token único que se almacena en la tabla de sesiones y se valida en cada endpoint protegido.
- **Logout:**  
  El endpoint de logout elimina la sesión activa asociada al token, invalidándolo.

### Gestión de Ítems (Cards)
- **CRUD completo:**  
  Permite crear, listar, obtener, actualizar y eliminar ítems.
- **Propiedades del ítem:**  
  Cada ítem (card) incluye:
    - **Título**
    - **Descripción** (opcional)
    - **Peso**
    - **Imagen:** codificada en Base64.
    - **Ubicación:** se almacenan las coordenadas (latitud y longitud) extraídas de la imagen, si están disponibles.

### Persistencia en MariaDB
- **Exposed y HikariCP:**  
  Se utiliza Exposed para mapear las entidades a la base de datos y HikariCP para la gestión de conexiones.
- **Creación/actualización de tablas:**  
  La función `createMissingTablesAndColumns` en `DatabaseFactory.kt` asegura que se creen o actualicen las tablas sin perder datos existentes.  
  Las tablas incluyen:
    - **UsersTable:** Para los usuarios.
    - **ItemsTable:** Para los ítems (cards), que ahora incluye columnas para `latitude` y `longitude`.
    - **SessionsTable:** Para gestionar las sesiones de usuario y los tokens JWT.

### Arquitectura Clean
- **Capa de Dominio:**  
  Contiene modelos, interfaces de repositorios y casos de uso.
- **Capa de Datos:**  
  Implementa los repositorios y define las tablas de la base de datos.
- **Capa de Presentación:**  
  Contiene las rutas de Ktor que exponen los endpoints, integrando la validación de JWT para proteger los endpoints sensibles.

---

## Estructura del Proyecto

```plaintext
mi-proyecto/
├── build.gradle.kts                # Configuración del proyecto con Gradle
├── docker-compose.yml              # (Opcional) Configuración para levantar contenedores (MariaDB y phpMyAdmin)
└── src
    └── main
        └── kotlin
            └── com
                └── example
                    ├── Application.kt                # Punto de entrada y configuración JWT
                    ├── data
                    │   ├── db
                    │   │   ├── DatabaseFactory.kt      # Configuración de la base de datos
                    │   │   ├── UsersTable.kt           # Definición de la tabla de usuarios
                    │   │   ├── ItemsTable.kt           # Definición de la tabla de ítems (incluye latitude y longitude)
                    │   │   └── SessionsTable.kt        # Definición de la tabla de sesiones
                    │   └── repositories
                    │       ├── UserRepositoryImpl.kt     # Implementación del repositorio de usuarios
                    │       ├── ItemRepositoryImpl.kt     # Implementación del repositorio de ítems
                    │       └── SessionRepositoryImpl.kt  # Implementación del repositorio de sesiones
                    ├── domain
                    │   ├── models
                    │   │   ├── User.kt                 # Modelo de usuario
                    │   │   ├── Item.kt                 # Modelo de ítem
                    │   │   └── Session.kt              # Modelo de sesión
                    │   ├── repositories
                    │   │   ├── UserRepository.kt       # Interfaz del repositorio de usuarios
                    │   │   ├── ItemRepository.kt       # Interfaz del repositorio de ítems
                    │   │   └── SessionRepository.kt    # Interfaz del repositorio de sesiones
                    │   └── usecases
                    │       ├── RegisterUserUseCase.kt  # Caso de uso para registrar usuarios
                    │       ├── LoginUserUseCase.kt     # Caso de uso para iniciar sesión
                    │       ├── GetUsersUseCase.kt      # Caso de uso para listar usuarios
                    │       ├── UpdateUserUseCase.kt    # Caso de uso para actualizar usuarios
                    │       ├── DeleteUserUseCase.kt    # Caso de uso para eliminar usuarios
                    │       ├── CreateItemUseCase.kt    # Caso de uso para crear ítems
                    │       ├── GetItemsUseCase.kt      # Caso de uso para listar ítems
                    │       ├── UpdateItemUseCase.kt    # Caso de uso para actualizar ítems
                    │       └── DeleteItemUseCase.kt    # Caso de uso para eliminar ítems
                    └── presentation
                        └── routes
                            ├── AuthRoutes.kt         # Endpoints de autenticación (registro, login, logout)
                            ├── UserRoutes.kt         # Endpoints para la gestión de usuarios
                            └── ItemRoutes.kt         # Endpoints para el CRUD de ítems (cards)
```

### Descripción de Directorios

- **data:**  
  Acceso a datos y definición de las tablas en la base de datos.
    - **db:** Configura la conexión (DatabaseFactory.kt) y define las tablas (UsersTable, ItemsTable y SessionsTable).
    - **repositories:** Implementa las interfaces de repositorios del dominio.

- **domain:**  
  Núcleo de la lógica de negocio.
    - **models:** Define las entidades (User, Item, Session).  
      *Nota: El modelo Item ahora incluye los campos `latitude` y `longitude`.*
    - **repositories:** Declara las interfaces para acceder a los datos.
    - **usecases:** Encapsula las operaciones de negocio (registro, login, CRUD de ítems, etc.).  
      *Nota: Los casos de uso para ítems ahora gestionan las coordenadas.*

- **presentation:**  
  Define los endpoints de la API utilizando Ktor.
    - **routes:** Contiene las rutas para autenticación, gestión de usuarios e ítems.

---

## Endpoints de la API

### Autenticación
- **Registro de Usuario:**  
  `POST /auth/register`  
  **Body:**
  ```json
  {
    "email": "test@example.com",
    "password": "miPasswordSecreta"
  }
  ```

- **Login de Usuario:**  
  `POST /auth/login`  
  **Body:**
  ```json
  {
    "email": "test@example.com",
    "password": "miPasswordSecreta"
  }
  ```
  **Respuesta exitosa:**
  ```json
  {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 1
  }
  ```

- **Logout:**  
  `POST /auth/logout`  
  **Headers:**
    - `Authorization: Bearer <token>`

### Gestión de Usuarios
- **Listar Usuarios:**  
  `GET /users`

- **Actualizar Usuario:**  
  `PUT /users/{id}`  
  **Body:**
  ```json
  {
    "email": "nuevo@example.com",
    "password": "nuevaPassword"
  }
  ```

- **Eliminar Usuario:**  
  `DELETE /users/{id}`

### Gestión de Ítems (Cards)
- **Crear Ítem:**  
  `POST /items`  
  **Body:**
  ```json
  {
    "title": "Título de la Card",
    "description": "Descripción de la card",
    "weight": 10,
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "userId": 1,
    "latitude": 37.77950668334961,
    "longitude": -3.754349946975708
  }
  ```

- **Listar Ítems:**  
  `GET /items`

- **Obtener Ítem por ID:**  
  `GET /items/{id}`

- **Actualizar Ítem:**  
  `PUT /items/{id}`  
  **Body:**
  ```json
  {
    "title": "Título actualizado",
    "description": "Nueva descripción",
    "weight": 12,
    "image": "data:image/png;base64,AAA...",
    "latitude": 37.77950668334961,
    "longitude": -3.754349946975708
  }
  ```

- **Eliminar Ítem:**  
  `DELETE /items/{id}`

---

## Configuración de la Base de Datos

- **Conexión:**  
  Se utiliza Exposed junto con HikariCP para conectarse a MariaDB.  
  Los parámetros por defecto son:
    - **JDBC_DATABASE_URL:** `jdbc:mariadb://localhost:3306/ktor_db`
    - **DB_USER:** `root`
    - **DB_PASSWORD:** `password`

- **Creación/Actualización de Tablas:**  
  La función `createMissingTablesAndColumns` en `DatabaseFactory.kt` se encarga de crear o actualizar las tablas sin perder los datos existentes.  
  Las tablas incluyen:
    - **UsersTable:** Para usuarios.
    - **ItemsTable:** Para ítems (cards), que ahora contiene columnas para `latitude` y `longitude`.
    - **SessionsTable:** Para gestionar las sesiones y tokens JWT.

---

## Configuración del Proyecto

### Dependencias Clave (build.gradle.kts)
```kotlin
dependencies {
    // Ktor core y Netty
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    // Content negotiation y serialización JSON
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.11")
    // Exposed y MariaDB
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.7")
    // HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")
    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")
    // JWT y autenticación en Ktor
    implementation("io.ktor:ktor-server-auth:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.0")
    implementation("com.auth0:java-jwt:3.18.2")
    // Testing
    testImplementation("io.ktor:ktor-server-tests:2.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.0")
}
```

### Docker Compose (Opcional)
```yaml
version: "3.8"
services:
  mariadb:
    image: mariadb:latest
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: ktor_db
      MYSQL_USER: root
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    environment:
      PMA_HOST: mariadb
      MYSQL_ROOT_PASSWORD: password
    ports:
      - "8081:80"
```

---

## Cómo Ejecutar el Proyecto

1. **Base de Datos:**
    - Si utilizas Docker, ejecuta:
      ```bash
      docker-compose up -d
      ```
    - Verifica que la base de datos y las tablas se hayan creado (puedes acceder a phpMyAdmin en [http://localhost:8081](http://localhost:8081)).

2. **Ejecutar la API:**
    - Compila y ejecuta el proyecto con Gradle:
      ```bash
      ./gradlew run
      ```
    - La aplicación se iniciará en el puerto **8080**.

---

## Endpoints de la API – Resumen y Pruebas

- **Autenticación:**  
  Permite registrar, iniciar sesión y cerrar sesión.
- **Gestión de Usuarios:**  
  Incluye endpoints para listar, actualizar y eliminar usuarios.
- **Gestión de Ítems (Cards):**  
  Los endpoints de ítems permiten crear, listar, obtener, actualizar y eliminar ítems.  
  *Nota:* Los ítems ahora incluyen campos para `latitude` y `longitude` que se almacenan si la imagen contiene datos EXIF o se envían explícitamente.

Para probar la API, puedes usar herramientas como Postman, asegurándote de que los cuerpos de las peticiones incluyan las coordenadas en los ítems cuando sea pertinente.

---

## Notas Finales

- **JWT y Sesiones:**
    - Cada login genera un token JWT único que se almacena en la tabla `sessions` junto con el `userId`.
    - Se eliminan las sesiones previas del usuario antes de generar un nuevo token.
    - Los endpoints protegidos validan el token enviado en la cabecera para asegurar el acceso.

- **Clean Architecture:**
    - La estructura en capas (dominio, datos, presentación) facilita el mantenimiento y la escalabilidad.
    - Los modelos y requests en la capa de dominio se han actualizado para incluir `latitude` y `longitude`.
