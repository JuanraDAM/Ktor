# Proyecto Ktor Sample

Este proyecto es un ejemplo de una API REST implementada en Kotlin utilizando Ktor, Exposed y MariaDB, siguiendo principios de Clean Architecture. La aplicación permite la gestión de usuarios y de ítems (cards) y ahora incorpora autenticación basada en JWT con gestión de sesiones. Cada vez que un usuario inicia sesión se genera un token único (JWT) que se almacena en una tabla de sesiones; este token se verifica en cada endpoint protegido y se invalida en caso de logout o si se reemplaza en un nuevo login.

## Características

- **Gestión de Usuarios**
    - Registro e inicio de sesión usando correo electrónico y contraseña.
    - Listado, actualización y eliminación de usuarios.
    - Cifrado de contraseñas con BCrypt.
    - Autenticación mediante JWT: cada login genera un token único que se almacena en una tabla de sesiones.
    - Logout: se elimina la sesión activa para invalidar el token.

- **Gestión de Ítems (Cards)**
    - Creación, listado, obtención, actualización y eliminación de ítems.
    - Cada ítem (card) tiene:
        - Título
        - Descripción (opcional)
        - Peso (por ejemplo, en gramos)
        - Imagen (codificada en Base64)
    - Los ítems se asocian a un usuario a través de su `userId`.

- **Persistencia en MariaDB**
    - Se utiliza Exposed junto con HikariCP para gestionar la base de datos.
    - Se usa `createMissingTablesAndColumns` para crear tablas o columnas que falten sin perder los datos existentes.

- **Arquitectura Clean**
    - El proyecto está organizado en capas:
        - **Dominio:** Modelos, repositorios y casos de uso.
        - **Datos:** Implementaciones de repositorios y definición de tablas (incluye la nueva tabla `sessions`).
        - **Presentación:** Rutas de Ktor para exponer la API, integradas con JWT para proteger endpoints.

## Estructura del Proyecto

```
mi-proyecto/
├── build.gradle.kts                # Configuración del proyecto con Gradle
├── docker-compose.yml              # Opcional: configuración para levantar contenedores (MariaDB y phpMyAdmin)
└── src
    └── main
        └── kotlin
            └── com
                └── example
                    ├── Application.kt                # Punto de entrada de la aplicación y configuración JWT
                    ├── data
                    │   ├── db
                    │   │   ├── DatabaseFactory.kt      # Configuración de la base de datos
                    │   │   ├── UsersTable.kt           # Definición de la tabla de usuarios
                    │   │   ├── ItemsTable.kt           # Definición de la tabla de ítems
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
                            ├── AuthRoutes.kt         # Endpoints de autenticación (registro, login y logout)
                            ├── UserRoutes.kt         # Endpoints para listar, actualizar y eliminar usuarios
                            └── ItemRoutes.kt         # Endpoints para el CRUD de ítems (cards)
```

## Requisitos

- **Java JDK 1.8+**
- **Kotlin 1.8+**
- **MariaDB** (o cualquier base de datos compatible con Exposed)
- **Gradle**
- Opcionalmente, **Docker** y **docker-compose** para levantar contenedores de MariaDB y phpMyAdmin

### Dependencias Clave (en build.gradle.kts)

```kotlin
dependencies {
    // Ktor core y Netty
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    // Plugin de content negotiation
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    // Serialización JSON con kotlinx
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.11")
    // Exposed y MariaDB
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.7")
    // HikariCP para conexión
    implementation("com.zaxxer:HikariCP:5.0.1")
    // Test
    testImplementation("io.ktor:ktor-server-tests:2.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.0")
    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")
    //Ktor Authentication y JWT
    implementation("io.ktor:ktor-server-auth:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.0")
    implementation("com.auth0:java-jwt:3.18.2")
}

```

## Configuración de la Base de Datos

La aplicación utiliza Exposed y HikariCP para conectarse a MariaDB. Los parámetros por defecto son:

- **JDBC_DATABASE_URL:** `jdbc:mariadb://localhost:3306/ktor_db`
- **DB_USER:** `root`
- **DB_PASSWORD:** `password`

En `DatabaseFactory.kt` se usa la función `createMissingTablesAndColumns` para crear las tablas que no existan (incluyendo la nueva tabla `sessions`), conservando los datos existentes al reiniciar la aplicación.

Si deseas levantar contenedores, puedes usar el siguiente `docker-compose.yml` (ajusta según tus necesidades):

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

## Cómo Ejecutar el Proyecto

1. **Base de Datos:**
    - Si usas Docker, en la raíz del proyecto ejecuta:
      ```bash
      docker-compose up -d
      ```
    - Verifica que la base de datos y las tablas se hayan creado (puedes acceder a phpMyAdmin en [http://localhost:8081](http://localhost:8081)).

2. **Ejecutar la Aplicación:**
    - Compila y ejecuta el proyecto con Gradle:
      ```bash
      ./gradlew run
      ```
    - La aplicación se iniciará en el puerto **8080**.

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
  **Funcionamiento:**
    - Se elimina cualquier sesión anterior para ese usuario.
    - Se crea una nueva sesión y se genera un token JWT (con claims `"email"` y `"sessionId"`).
    - Se guarda el token en la tabla `sessions` y se devuelve en la respuesta:
      ```json
      { "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..." }
      ```

- **Logout:**  
  `POST /auth/logout`  
  **Headers:**
    - `Authorization: Bearer <token>`
      **Funcionamiento:**
    - Se elimina la sesión asociada al token recibido.
    - La respuesta debe indicar que la sesión se cerró (por ejemplo, "Sesión cerrada").

### Gestión de Usuarios (Proteger con JWT)
- **Listar Usuarios:**  
  `GET /users`  
  **Respuesta esperada:**
  ```json
  {
    "users": [
      {
        "id": 1,
        "email": "user1@example.com",
        "password": "$2a$10$..."
      },
      {
        "id": 2,
        "email": "user2@example.com",
        "password": "$2a$10$..."
      }
    ]
  }
  ```

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

### Gestión de Ítems (Cards) (Proteger con JWT)
- **Crear Ítem:**  
  `POST /items`  
  **Body:**
  ```json
  {
    "title": "Título de la Card",
    "description": "Descripción de la card",
    "weight": 10,
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "userId": 1
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
    "image": "data:image/png;base64,AAA..."
  }
  ```

- **Eliminar Ítem:**  
  `DELETE /items/{id}`

## Notas

- **Persistencia:**  
  Se utiliza `createMissingTablesAndColumns` en `DatabaseFactory.kt` para que los datos existentes no se pierdan al reiniciar la aplicación.

- **JWT y Sesiones:**
    - Cada login genera un token JWT único que se almacena en una tabla de sesiones junto con el ID del usuario.
    - Antes de crear una nueva sesión, se eliminan las sesiones previas del usuario para garantizar que solo haya un token activo.
    - Todos los endpoints protegidos verifican que el token enviado en la cabecera coincide con el token almacenado en la sesión en la base de datos.
    - Si se modifica el token en la base de datos (por ejemplo, a través de phpMyAdmin), la validación fallará y el acceso será denegado.
    - El endpoint de logout elimina la sesión activa asociada al token, invalidándolo.

- **Clean Architecture:**  
  El proyecto está organizado en capas (dominio, datos y presentación) para facilitar el mantenimiento y la escalabilidad.
