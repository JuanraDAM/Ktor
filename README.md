# Proyecto Ktor Sample

Este proyecto es un ejemplo de una API REST implementada en Kotlin utilizando Ktor, Exposed y MariaDB, siguiendo principios de Clean Architecture. La aplicación permite la gestión de usuarios e ítems (cards) e incorpora autenticación basada en JWT con gestión de sesiones. Cada vez que un usuario inicia sesión se genera un token único (JWT) que se almacena en la tabla de sesiones; este token se valida en cada endpoint protegido y se invalida en caso de logout o cuando se produce un nuevo login.

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
  - **Imagen:**  
    Ahora la API recibe la imagen codificada en Base64, la decodifica y guarda el fichero físicamente en el servidor. Se almacena en la base de datos la ruta (URL) del fichero en lugar de la cadena Base64 original. Además, las imágenes se organizan en carpetas específicas por usuario, por ejemplo, en `uploads/images/{userId}`.
  - **Ubicación:**  
    Se almacenan las coordenadas (latitud y longitud) que pueden extraerse de la imagen (por ejemplo, datos EXIF) o enviarse explícitamente.

  En las operaciones de actualización, si se envía una nueva imagen, la API elimina el fichero antiguo y guarda el nuevo. Al eliminar un ítem, se borra el fichero físico y, si el directorio del usuario queda vacío, se elimina también.

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
  Define los endpoints de Ktor que exponen la API, integrando la validación de JWT para proteger los endpoints sensibles.
- **Utilidades:**  
  Se ha añadido la carpeta `utils` que contiene el fichero `FileUtil.kt`, responsable del procesamiento de imágenes: decodificar Base64, guardar ficheros en disco organizados por usuario y gestionar la eliminación de imágenes (y directorios vacíos).

---

## Estructura del Proyecto

```plaintext
example/
├── Application.kt
├── data
│   ├── db
│   │   ├── DatabaseFactory.kt
│   │   ├── ItemsTable.kt
│   │   ├── SessionsTable.kt
│   │   └── UsersTable.kt
│   └── repositories
│       ├── ItemRepositoryImpl.kt
│       ├── SessionRepositoryImpl.kt
│       └── UserRepositoryImpl.kt
├── domain
│   ├── Cards
│   │   ├── CreateItemRequest.kt
│   │   └── UpdateItemRequest.kt
│   ├── models
│   │   ├── Item.kt
│   │   ├── Session.kt
│   │   └── User.kt
│   ├── repositories
│   │   ├── ItemRepository.kt
│   │   ├── SessionRepository.kt
│   │   └── UserRepository.kt
│   └── usecases
│       ├── CreateItemUseCase.kt
│       ├── DeleteItemUseCase.kt
│       ├── DeleteUserUseCase.kt
│       ├── GetItemsUseCase.kt
│       ├── GetUsersUseCase.kt
│       ├── LoginUserUseCase.kt
│       ├── RegisterUserUseCase.kt
│       ├── UpdateItemUseCase.kt
│       └── UpdateUserUseCase.kt
├── presentation
│   └── routes
│       ├── AuthRoutes.kt
│       ├── ItemRoutes.kt
│       └── UserRoutes.kt
└── utils
    └── FileUtil.kt
```

### Descripción de Directorios

- **data:**  
  Acceso a datos y definición de las tablas en la base de datos:
  - **db:** Configura la conexión (DatabaseFactory.kt) y define las tablas (UsersTable, ItemsTable y SessionsTable).
  - **repositories:** Implementa las interfaces definidas en la capa de dominio.
- **domain:**  
  Núcleo de la lógica de negocio:
  - **models:** Define las entidades (User, Item, Session).  
    *Nota: El modelo Item ahora incluye `latitude` y `longitude`.*
  - **repositories:** Declara las interfaces para acceder a los datos.
  - **usecases:** Encapsula las operaciones de negocio, incluyendo el procesamiento de imágenes en ítems.
- **presentation:**  
  Define los endpoints de la API utilizando Ktor.
  - **routes:** Contiene las rutas para autenticación, gestión de usuarios e ítems.
- **utils:**  
  Contiene utilidades generales, incluyendo `FileUtil.kt`, que gestiona la decodificación de imágenes Base64, almacenamiento en disco en carpetas por usuario y eliminación de ficheros y directorios vacíos.

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
  *Nota:* Se espera que el campo `image` contenga la cadena Base64 de la imagen. Se recomienda enviar únicamente la cadena Base64 (puedes incluir el prefijo `data:image/png;base64,` si lo deseas, pero la lógica actual guarda la imagen con extensión `.png`).
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
  *Si se envía un nuevo valor para `image`, la API elimina el fichero antiguo y guarda el nuevo, actualizando la ruta en la base de datos.*

- **Eliminar Ítem:**  
  `DELETE /items/{id}`  
  *La API elimina el registro, borra el fichero físico y, si el directorio del usuario queda vacío, se elimina también.*

---

## Configuración de la Base de Datos

- **Conexión:**  
  Se utiliza Exposed junto con HikariCP para conectarse a MariaDB.  
  Parámetros por defecto:
  - **JDBC_DATABASE_URL:** `jdbc:mariadb://localhost:3306/ktor_db`
  - **DB_USER:** `root`
  - **DB_PASSWORD:** `password`

- **Creación/Actualización de Tablas:**  
  La función `createMissingTablesAndColumns` en `DatabaseFactory.kt` se encarga de crear o actualizar las tablas sin perder datos.  
  Las tablas incluyen:
  - **UsersTable:** Para usuarios.
  - **ItemsTable:** Para ítems (cards), que ahora incluye `latitude` y `longitude`.
  - **SessionsTable:** Para gestionar sesiones y tokens JWT.

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
  - Si usas Docker, ejecuta:
    ```bash
    docker-compose up -d
    ```
  - Verifica la base de datos y las tablas (puedes acceder a phpMyAdmin en [http://localhost:8081](http://localhost:8081)).

2. **Ejecutar la API:**
  - Compila y ejecuta el proyecto con Gradle:
    ```bash
    ./gradlew run
    ```
  - La aplicación se iniciará en el puerto **8080**.

---

## Endpoints – Resumen y Pruebas

- **Autenticación:**  
  Registro, login y logout.
- **Gestión de Usuarios:**  
  Listar, actualizar y eliminar usuarios.
- **Gestión de Ítems (Cards):**  
  Los endpoints permiten crear, listar, obtener, actualizar y eliminar ítems.  
  *Nota:* Los ítems ahora incluyen `latitude` y `longitude` y gestionan el almacenamiento físico de imágenes (Base64 → fichero), organizándolas por usuario. Al actualizar o eliminar ítems se eliminan los ficheros antiguos y, si es necesario, el directorio del usuario.

Puedes probar la API con Postman u otra herramienta, asegurándote de enviar cadenas Base64 válidas en el campo `image`.

---

## Notas Finales

- **JWT y Sesiones:**
  - Cada login genera un token JWT único almacenado en la tabla `sessions` junto con el `userId`.
  - Se eliminan sesiones previas antes de generar un nuevo token.
  - Los endpoints protegidos validan el token enviado en la cabecera.

- **Manejo de Imágenes:**
  - La API decodifica la imagen en Base64 y guarda el fichero en `uploads/images/{userId}`.
  - La base de datos almacena la ruta del fichero en lugar de la cadena Base64.
  - Al actualizar un ítem, se elimina la imagen antigua; al eliminar un ítem, se elimina el fichero y, si el directorio queda vacío, se limpia.
  - Actualmente se utiliza la extensión `.png` para todas las imágenes, aunque la lógica puede ampliarse para detectar el tipo de imagen.

- **Clean Architecture:**
  - La estructura en capas (dominio, datos, presentación y utilidades) facilita el mantenimiento y la escalabilidad.
  - Los modelos y casos de uso se han actualizado para incluir la gestión de coordenadas y la lógica de manejo de imágenes.

