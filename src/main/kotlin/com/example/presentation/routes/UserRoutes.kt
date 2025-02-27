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
        // Listar usuarios
        get {
            val users = getUsersUseCase.invoke()
            call.respond(UsersResponse(users))
        }

        // Actualizar usuario
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Id de usuario inválido")
                return@put
            }
            val request = call.receive<UpdateUserRequest>()
            // Se asume que en el use case se cifra la contraseña si se envía
            val updated = updateUserUseCase.invoke(id, User(0, request.email, request.password))
            if (updated) {
                call.respond(HttpStatusCode.OK, "Usuario actualizado")
            } else {
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            }
        }



        // Eliminar usuario
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Id de usuario inválido")
                return@delete
            }
            val deleted = deleteUserUseCase.invoke(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Usuario eliminado")
            } else {
                call.respond(HttpStatusCode.NotFound, "Usuario no encontrado")
            }
        }
    }
}
