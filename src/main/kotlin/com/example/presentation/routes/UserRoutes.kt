package com.example.presentation.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import com.example.domain.usecases.UpdateUserUseCase
import com.example.domain.usecases.DeleteUserUseCase
import com.example.domain.usecases.GetUsersUseCase
import com.example.domain.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(val email: String, val password: String)

@Serializable
data class UsersResponse(val users: List<User>)

fun Route.userRoutes(
    updateUserUseCase: UpdateUserUseCase,
    deleteUserUseCase: DeleteUserUseCase,
    getUsersUseCase: GetUsersUseCase
) {
    route("/users") {
        get {
            call.application.environment.log.info("GET /users invocado")
            val users = getUsersUseCase.invoke()
            call.application.environment.log.info("Usuarios recuperados: ${users.size}")
            call.respond(UsersResponse(users))
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            call.application.environment.log.info("PUT /users/{id} invocado con id: $id")
            if (id == null) {
                call.application.environment.log.info("Id de usuario inválido: $id")
                call.respond(HttpStatusCode.BadRequest, "Id de usuario inválido")
                return@put
            }
            val request = call.receive<UpdateUserRequest>()
            call.application.environment.log.info("Datos recibidos para actualizar usuario con id: $id, email: ${request.email}")
            val updated = updateUserUseCase.invoke(id, User(0, request.email, request.password))
            if (updated) {
                call.application.environment.log.info("Usuario actualizado exitosamente con id: $id")
                call.respond(HttpStatusCode.OK, "Usuario actualizado")
            } else {
                call.application.environment.log.info("Usuario no encontrado para actualización con id: $id")
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            call.application.environment.log.info("DELETE /users/{id} invocado con id: $id")
            if (id == null) {
                call.application.environment.log.info("Id de usuario inválido: $id")
                call.respond(HttpStatusCode.BadRequest, "Id de usuario inválido")
                return@delete
            }
            val deleted = deleteUserUseCase.invoke(id)
            if (deleted) {
                call.application.environment.log.info("Usuario eliminado exitosamente con id: $id")
                call.respond(HttpStatusCode.OK, "Usuario eliminado")
            } else {
                call.application.environment.log.info("Usuario no encontrado para eliminación con id: $id")
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            }
        }
    }
}
