package com.example.presentation.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository
import com.example.domain.usecases.CreateItemUseCase
import com.example.domain.usecases.GetItemsUseCase
import com.example.domain.usecases.UpdateItemUseCase
import com.example.domain.usecases.DeleteItemUseCase
import kotlinx.serialization.Serializable
import io.ktor.http.HttpStatusCode

@Serializable
data class CreateItemRequest(val title: String, val description: String?, val weight: Int, val image: String, val userId: Int)

@Serializable
data class UpdateItemRequest(val title: String?, val description: String?, val weight: Int?, val image: String?)

@Serializable
data class ItemsResponse(val items: List<Item>)

fun Route.itemRoutes(itemRepository: ItemRepository) {
    val createItemUseCase = CreateItemUseCase(itemRepository)
    val getItemsUseCase = GetItemsUseCase(itemRepository)
    val updateItemUseCase = UpdateItemUseCase(itemRepository)
    val deleteItemUseCase = DeleteItemUseCase(itemRepository)

    route("/items") {
        // Crear ítem
        post {
            val request = call.receive<CreateItemRequest>()
            val itemId = createItemUseCase.invoke(
                Item(
                    id = 0,
                    title = request.title,
                    description = request.description,
                    weight = request.weight,
                    image = request.image,
                    userId = request.userId
                )
            )
            call.respond(HttpStatusCode.Created, "Ítem creado con id: $itemId")
        }

        // Obtener todos los ítems
        get {
            val items = getItemsUseCase.invoke()
            call.respond(ItemsResponse(items))
        }

        // Obtener un ítem por id
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@get
            }
            val item = itemRepository.getItemById(id)
            if (item == null) {
                call.respond(HttpStatusCode.NotFound, "Ítem no encontrado")
            } else {
                call.respond(item)
            }
        }

        // Actualizar un ítem
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@put
            }
            val request = call.receive<UpdateItemRequest>()
            val existingItem = itemRepository.getItemById(id)
            if (existingItem == null) {
                call.respond(HttpStatusCode.NotFound, "Ítem no encontrado")
                return@put
            }
            val updatedItem = existingItem.copy(
                title = request.title ?: existingItem.title,
                description = request.description ?: existingItem.description,
                weight = request.weight ?: existingItem.weight,
                image = request.image ?: existingItem.image
            )
            val updated = updateItemUseCase.invoke(id, updatedItem)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Ítem actualizado")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error al actualizar el ítem")
            }
        }

        // Eliminar un ítem
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@delete
            }
            val deleted = deleteItemUseCase.invoke(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Ítem eliminado")
            } else {
                call.respond(HttpStatusCode.NotFound, "Ítem no encontrado")
            }
        }
    }
}
