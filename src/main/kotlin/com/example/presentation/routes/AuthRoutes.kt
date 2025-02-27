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

// Nuevo request para recuperación de contraseña
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
            val request = call.receive<RegisterRequest>()
            val userId = registerUserUseCase.invoke(User(0, request.email, request.password))
            if (userId == null) {
                call.respond(HttpStatusCode.Conflict, "El usuario ya existe")
            } else {
                call.respond(HttpStatusCode.Created, "Usuario registrado con id: $userId")
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = loginUserUseCase.invoke(request.email, request.password)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales inválidas")
                return@post
            }
            // Elimina sesiones anteriores para este usuario
            sessionRepository.deleteSessionsByUserId(user.id)

            // Inserta una sesión temporal con token vacío para obtener el ID de sesión
            val tempSession = sessionRepository.createSession(user.id, "")

            // Genera el token JWT incluyendo el ID de sesión
            val token = JWT.create()
                .withClaim("email", user.email)
                .withClaim("sessionId", tempSession.id)
                .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .sign(Algorithm.HMAC256("secret"))

            // Actualiza la sesión con el token generado
            sessionRepository.updateSessionToken(tempSession.id, token)

            // Devuelve el token y el userId en la respuesta
            call.respond(HttpStatusCode.OK, LoginResponse(token, user.id))

        }


        // Nuevo endpoint para recuperar (actualizar) la contraseña sin token
        post("/recover") {
            val request = call.receive<RecoverPasswordRequest>()
            val user = userRepository.getUserByEmail(request.email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            } else {
                // Actualiza la contraseña creando un nuevo objeto User con el nuevo password
                val updated = updateUserUseCase.invoke(user.id, User(user.id, user.email, request.newPassword))
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Contraseña actualizada")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Error al actualizar la contraseña")
                }
            }
        }

        // Logout: requiere autenticación
        authenticate("auth-jwt") {
            post("/logout") {
                val authHeader = call.request.headers["Authorization"] ?: ""
                val token = authHeader.removePrefix("Bearer ").trim()
                if (token.isEmpty()) {
                    call.respond(HttpStatusCode.Unauthorized, "No se proporcionó token")
                    return@post
                }
                val deleted = sessionRepository.deleteSession(token)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, "Sesión cerrada")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Sesión no encontrada")
                }
            }
        }
    }
}
