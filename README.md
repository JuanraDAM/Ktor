Aquí tienes un ejemplo completo de un README en formato Markdown para documentar tu proyecto:

---

# Proyecto Ktor Sample

Este proyecto es un ejemplo de una API REST implementada en Kotlin con Ktor, Exposed y MariaDB, siguiendo principios de Clean Architecture. La aplicación permite la gestión de usuarios y de ítems (cards). Los usuarios se registran e inician sesión con su correo y contraseña (que se cifra con BCrypt), y se pueden listar, actualizar y eliminar. Los ítems (cards) contienen título, descripción, peso e imagen en formato Base64 y se asocian a un usuario.

## Características

- **Gestión de Usuarios**
    - Registro e inicio de sesión usando correo electrónico y contraseña.
    - Listado, actualización y eliminación de usuarios.
    - Cifrado de contraseñas con BCrypt.

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
    - La base de datos se configura de modo que los datos existentes se conservan al reiniciar la aplicación.

- **Arquitectura Clean**
    - El proyecto está organizado en capas:
        - **Dominio:** Modelos, repositorios y casos de uso.
        - **Datos:** Implementaciones de los repositorios y definición de tablas.
        - **Presentación:** Rutas de Ktor para exponer la API.

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
                    ├── Application.kt                # Punto de entrada de la aplicación
                    ├── data
                    │   ├── db
                    │   │   ├── DatabaseFactory.kt      # Configuración de la base de datos
                    │   │   ├── UsersTable.kt           # Definición de la tabla de usuarios
                    │   │   └── ItemsTable.kt           # Definición de la tabla de ítems
                    │   └── repositories
                    │       ├── UserRepositoryImpl.kt     # Implementación del repositorio de usuarios
                    │       └── ItemRepositoryImpl.kt     # Implementación del repositorio de ítems
                    ├── domain
                    │   ├── models
                    │   │   ├── User.kt                 # Modelo de usuario
                    │   │   └── Item.kt                 # Modelo de ítem
                    │   ├── repositories
                    │   │   ├── UserRepository.kt       # Interfaz del repositorio de usuarios
                    │   │   └── ItemRepository.kt       # Interfaz del repositorio de ítems
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
                            ├── AuthRoutes.kt         # Endpoints de autenticación (registro y login)
                            ├── UserRoutes.kt         # Endpoints para listar, actualizar y eliminar usuarios
                            └── ItemRoutes.kt         # Endpoints para el CRUD de ítems (cards)
```

## Requisitos

- **Java JDK 1.8+**
- **Kotlin 1.8+**
- **MariaDB** (o cualquier base de datos compatible con Exposed)
- **Gradle**
- Opcionalmente, **Docker** y **docker-compose** para levantar contenedores de MariaDB y phpMyAdmin

## Configuración de la Base de Datos

La aplicación utiliza Exposed con HikariCP para conectarse a MariaDB. Los parámetros por defecto son:

- **JDBC_DATABASE_URL:** `jdbc:mariadb://localhost:3306/ktor_db`
- **DB_USER:** `root`
- **DB_PASSWORD:** `password`

En el archivo `DatabaseFactory.kt` se utiliza la función `createMissingTablesAndColumns` para asegurarse de que, al iniciar la aplicación, se creen únicamente las tablas o columnas que aún no existan, de modo que los datos previos se conserven.

Si deseas levantar contenedores, puedes usar el siguiente `docker-compose.yml` (modifícalo según tus necesidades):

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

### Gestión de Usuarios
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
  Se utiliza `createMissingTablesAndColumns` para que los datos existentes no se pierdan al reiniciar la aplicación.

- **Seguridad:**  
  Las contraseñas se cifran con BCrypt.  
  En esta versión la autenticación se realiza de forma básica (registro y login sin tokens).

- **Clean Architecture:**  
  El código se organiza en capas (dominio, datos y presentación) para facilitar el mantenimiento y la escalabilidad.
