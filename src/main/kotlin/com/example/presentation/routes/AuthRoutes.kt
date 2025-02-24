package com.example.presentation.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import com.example.domain.models.User
import com.example.domain.repositories.UserRepository
import com.example.domain.usecases.RegisterUserUseCase
import com.example.domain.usecases.LoginUserUseCase
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

fun Route.authRoutes(userRepository: UserRepository) {
    val registerUserUseCase = RegisterUserUseCase(userRepository)
    val loginUserUseCase = LoginUserUseCase(userRepository)

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            // Crea el usuario usando email y password
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
                call.respond(HttpStatusCode.OK, "Login exitoso")
            }
        }
    }
}
