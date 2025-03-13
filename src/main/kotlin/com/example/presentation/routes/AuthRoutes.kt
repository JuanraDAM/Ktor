package com.example.presentation.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import com.example.domain.models.User
import com.example.domain.repositories.UserRepository
import com.example.domain.repositories.SessionRepository
import com.example.domain.usecases.RegisterUserUseCase
import com.example.domain.usecases.LoginUserUseCase
import com.example.domain.usecases.UpdateUserUseCase
import kotlinx.serialization.Serializable
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.authenticate
import java.util.Date

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val userId: Int)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RecoverPasswordRequest(val email: String, val newPassword: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    sessionRepository: SessionRepository,
    updateUserUseCase: UpdateUserUseCase
) {
    val registerUserUseCase = RegisterUserUseCase(userRepository)
    val loginUserUseCase = LoginUserUseCase(userRepository)

    route("/auth") {
        post("/register") {
            // Log inicio de la petición
            call.application.environment.log.info("POST /auth/register invocado")
            val request = call.receive<RegisterRequest>()
            call.application.environment.log.info("Datos recibidos para registro: email=${request.email}")
            val userId = registerUserUseCase.invoke(User(0, request.email, request.password))
            if (userId == null) {
                call.application.environment.log.info("Registro fallido: el usuario ya existe: ${request.email}")
                call.respond(HttpStatusCode.Conflict, "El usuario ya existe")
            } else {
                call.application.environment.log.info("Usuario registrado exitosamente con id: $userId")
                call.respond(HttpStatusCode.Created, "Usuario registrado con id: $userId")
            }
        }

        post("/login") {
            call.application.environment.log.info("POST /auth/login invocado")
            val request = call.receive<LoginRequest>()
            call.application.environment.log.info("Intento de login para email: ${request.email}")
            val user = loginUserUseCase.invoke(request.email, request.password)
            if (user == null) {
                call.application.environment.log.info("Login fallido: credenciales inválidas para email: ${request.email}")
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                return@post
            }
            // Elimina sesiones anteriores para este usuario
            sessionRepository.deleteSessionsByUserId(user.id)
            call.application.environment.log.info("Sesiones anteriores eliminadas para usuario id: ${user.id}")

            // Crea una sesión temporal
            val tempSession = sessionRepository.createSession(user.id, "")
            call.application.environment.log.info("Sesión temporal creada con id: ${tempSession.id} para usuario id: ${user.id}")

            // Genera el token JWT incluyendo el ID de sesión
            val token = JWT.create()
                .withClaim("email", user.email)
                .withClaim("sessionId", tempSession.id)
                .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .sign(Algorithm.HMAC256("secret"))

            // Actualiza la sesión con el token generado
            sessionRepository.updateSessionToken(tempSession.id, token)
            call.application.environment.log.info("Token JWT generado y sesión actualizada para usuario id: ${user.id}")

            // Devuelve el token y el userId en la respuesta
            call.respond(HttpStatusCode.OK, LoginResponse(token, user.id))
        }

        post("/recover") {
            call.application.environment.log.info("POST /auth/recover invocado")
            val request = call.receive<RecoverPasswordRequest>()
            call.application.environment.log.info("Intento de recuperación de contraseña para email: ${request.email}")
            val user = userRepository.getUserByEmail(request.email)
            if (user == null) {
                call.application.environment.log.info("Recuperación fallida: usuario no encontrado para email: ${request.email}")
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            } else {
                val updated = updateUserUseCase.invoke(user.id, User(user.id, user.email, request.newPassword))
                if (updated) {
                    call.application.environment.log.info("Contraseña actualizada exitosamente para usuario id: ${user.id}")
                    call.respond(HttpStatusCode.OK, "Contraseña actualizada")
                } else {
                    call.application.environment.log.info("Error al actualizar la contraseña para usuario id: ${user.id}")
                    call.respond(HttpStatusCode.InternalServerError, "Error al actualizar la contraseña")
                }
            }
        }

        // Logout: requiere autenticación
        authenticate("auth-jwt") {
            post("/logout") {
                call.application.environment.log.info("POST /auth/logout invocado")
                val authHeader = call.request.headers["Authorization"] ?: ""
                val token = authHeader.removePrefix("Bearer ").trim()
                if (token.isEmpty()) {
                    call.application.environment.log.info("Logout fallido: no se proporcionó token")
                    call.respond(HttpStatusCode.Unauthorized, "No se proporcionó token")
                    return@post
                }
                val deleted = sessionRepository.deleteSession(token)
                if (deleted) {
                    call.application.environment.log.info("Logout exitoso: sesión eliminada para token: $token")
                    call.respond(HttpStatusCode.OK, "Sesión cerrada")
                } else {
                    call.application.environment.log.info("Logout fallido: sesión no encontrada para token: $token")
                    call.respond(HttpStatusCode.NotFound, "Sesión no encontrada")
                }
            }
        }
    }
}
