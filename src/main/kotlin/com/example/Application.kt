package com.example

import com.example.data.db.DatabaseFactory
import com.example.data.repositories.ItemRepositoryImpl
import com.example.data.repositories.UserRepositoryImpl
import com.example.data.repositories.SessionRepositoryImpl
import com.example.presentation.routes.authRoutes
import com.example.presentation.routes.itemRoutes
import com.example.presentation.routes.userRoutes
import com.example.domain.usecases.UpdateUserUseCase
import com.example.domain.usecases.DeleteUserUseCase
import com.example.domain.usecases.GetUsersUseCase
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main() {
    // Inicializa la base de datos sin dropear datos existentes
    DatabaseFactory.init()

    val userRepository = UserRepositoryImpl()
    val itemRepository = ItemRepositoryImpl()
    val sessionRepository = SessionRepositoryImpl()

    // Casos de uso para usuarios
    val updateUserUseCase = UpdateUserUseCase(userRepository)
    val deleteUserUseCase = DeleteUserUseCase(userRepository)
    val getUsersUseCase = GetUsersUseCase(userRepository)

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        install(Authentication) {
            jwt("auth-jwt") {
                realm = "ktor-sample"
                verifier(JWT.require(Algorithm.HMAC256("secret")).build())
                validate { credential ->
                    // Extraemos las claims necesarias
                    val email = credential.payload.getClaim("email").asString()
                    val sessionId = credential.payload.getClaim("sessionId").asInt() ?: return@validate null
                    if (email.isEmpty()) return@validate null

                    // Extraemos el token original de la cabecera "Authorization"
                    val authHeader = this.request.headers["Authorization"] ?: ""
                    val tokenFromHeader = authHeader.removePrefix("Bearer ").trim()

                    // Buscamos el usuario y la sesión en la BBDD
                    val user = userRepository.getUserByEmail(email) ?: return@validate null
                    val session = sessionRepository.getSessionById(sessionId) ?: return@validate null

                    // Verificamos que la sesión pertenece al usuario y que el token coincide
                    if (session.userId == user.id && session.token == tokenFromHeader) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }

        routing {
            // Rutas públicas: registro, login, logout y recuperación de contraseña
            authRoutes(userRepository, sessionRepository, updateUserUseCase)
            // Rutas protegidas
            authenticate("auth-jwt") {
                userRoutes(updateUserUseCase, deleteUserUseCase, getUsersUseCase)
                itemRoutes(itemRepository)
            }
        }
    }.start(wait = true)
}
