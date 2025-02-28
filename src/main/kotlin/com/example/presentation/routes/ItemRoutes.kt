package com.example.presentation.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.HttpStatusCode
import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository
import com.example.domain.usecases.CreateItemUseCase
import com.example.domain.usecases.GetItemsUseCase
import com.example.domain.usecases.UpdateItemUseCase
import com.example.domain.usecases.DeleteItemUseCase
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val description: String?,
    val weight: Int,
    val image: String,
    val userId: Int,
    // Se agregan estos nuevos campos:
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class UpdateItemRequest(
    val title: String? = null,
    val description: String? = null,
    val weight: Int? = null,
    val image: String? = null,
    // Agrega también estos campos opcionales:
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class ItemsResponse(val items: List<Item>)

fun Route.itemRoutes(itemRepository: ItemRepository) {
    val createItemUseCase = CreateItemUseCase(itemRepository)
    val getItemsUseCase = GetItemsUseCase(itemRepository)
    val updateItemUseCase = UpdateItemUseCase(itemRepository)
    val deleteItemUseCase = DeleteItemUseCase(itemRepository)

    route("/items") {
        post {
            try {
                val request = call.receive<CreateItemRequest>()
                val newItem: Item = createItemUseCase.invoke(request)
                call.respond(HttpStatusCode.Created, newItem)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error al crear ítem: ${e.message}")
            }
        }

        get {
            val items = getItemsUseCase.invoke()
            call.respond(ItemsResponse(items))
        }

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
                image = request.image ?: existingItem.image,
                latitude = request.latitude ?: existingItem.latitude,
                longitude = request.longitude ?: existingItem.longitude
            )
            val updated = updateItemUseCase.invoke(id, updatedItem)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Ítem actualizado")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Error al actualizar el ítem")
            }
        }

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
