package com.example

import com.example.data.db.DatabaseFactory
import com.example.data.repositories.ItemRepositoryImpl
import com.example.data.repositories.UserRepositoryImpl
import com.example.presentation.routes.authRoutes
import com.example.presentation.routes.itemRoutes
import com.example.presentation.routes.userRoutes
import com.example.domain.usecases.UpdateUserUseCase
import com.example.domain.usecases.DeleteUserUseCase
import com.example.domain.usecases.GetUsersUseCase
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing

fun main() {
    DatabaseFactory.init()

    val userRepository = UserRepositoryImpl()
    val itemRepository = ItemRepositoryImpl()

    // Casos de uso para usuarios
    val updateUserUseCase = UpdateUserUseCase(userRepository)
    val deleteUserUseCase = DeleteUserUseCase(userRepository)
    val getUsersUseCase = GetUsersUseCase(userRepository)

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            authRoutes(userRepository)
            userRoutes(updateUserUseCase, deleteUserUseCase, getUsersUseCase)
            itemRoutes(itemRepository)
        }
    }.start(wait = true)
}
