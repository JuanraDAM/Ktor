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
import kotlinx.serialization.Serializable
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.authenticate
import java.util.Date

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    sessionRepository: SessionRepository
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
            } else {
                // Elimina sesiones anteriores para este usuario
                sessionRepository.deleteSessionsByUserId(user.id)

                // Generar token JWT con claim "email" y "sessionId"
                // Se crea primero una sesión temporal
                val tempSession = sessionRepository.createSession(user.id, "")
                val token = JWT.create()
                    .withClaim("email", user.email)
                    .withClaim("sessionId", tempSession.id)
                    .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 minutos de expiración
                    .sign(Algorithm.HMAC256("secret"))

                // Actualizar la sesión con el token generado
                sessionRepository.updateSessionToken(tempSession.id, token)

                // Responder con el token
                call.respond(HttpStatusCode.OK, mapOf("token" to token))
            }
        }

        // Logout: Eliminar la sesión actual (requiere autenticación)
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
