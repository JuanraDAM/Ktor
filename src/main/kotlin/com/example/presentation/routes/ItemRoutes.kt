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
            call.application.environment.log.info("POST /items invocado")
            try {
                val request = call.receive<CreateItemRequest>()
                call.application.environment.log.info("Datos recibidos para crear ítem: title=${request.title}, userId=${request.userId}")
                val newItem: Item = createItemUseCase.invoke(request)
                call.application.environment.log.info("Ítem creado exitosamente con id: ${newItem.id}")
                call.respond(HttpStatusCode.Created, newItem)
            } catch (e: Exception) {
                call.application.environment.log.error("Error al crear ítem: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Error al crear ítem: ${e.message}")
            }
        }

        get {
            call.application.environment.log.info("GET /items invocado")
            val items = getItemsUseCase.invoke()
            call.application.environment.log.info("Número de ítems recuperados: ${items.size}")
            call.respond(ItemsResponse(items))
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            call.application.environment.log.info("GET /items/{id} invocado con id: $id")
            if (id == null) {
                call.application.environment.log.info("Id de ítem inválido: $id")
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@get
            }
            val item = itemRepository.getItemById(id)
            if (item == null) {
                call.application.environment.log.info("Ítem no encontrado con id: $id")
                call.respond(HttpStatusCode.NotFound, "Ítem no encontrado")
            } else {
                call.application.environment.log.info("Ítem encontrado con id: $id")
                call.respond(item)
            }
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            call.application.environment.log.info("PUT /items/{id} invocado con id: $id")
            if (id == null) {
                call.application.environment.log.info("Id de ítem inválido: $id")
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@put
            }
            val request = call.receive<UpdateItemRequest>()
            call.application.environment.log.info("Datos recibidos para actualizar ítem con id: $id")
            val existingItem = itemRepository.getItemById(id)
            if (existingItem == null) {
                call.application.environment.log.info("Ítem no encontrado con id: $id")
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
                call.application.environment.log.info("Ítem actualizado exitosamente con id: $id")
                call.respond(HttpStatusCode.OK, "Ítem actualizado")
            } else {
                call.application.environment.log.error("Error al actualizar el ítem con id: $id")
                call.respond(HttpStatusCode.InternalServerError, "Error al actualizar el ítem")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            call.application.environment.log.info("DELETE /items/{id} invocado con id: $id")
            if (id == null) {
                call.application.environment.log.info("Id de ítem inválido: $id")
                call.respond(HttpStatusCode.BadRequest, "Id de ítem inválido")
                return@delete
            }
            val deleted = deleteItemUseCase.invoke(id)
            if (deleted) {
                call.application.environment.log.info("Ítem eliminado exitosamente con id: $id")
                call.respond(HttpStatusCode.OK, "Ítem eliminado")
            } else {
                call.application.environment.log.info("Ítem no encontrado para eliminación con id: $id")
                call.respond(HttpStatusCode.NotFound, "Ítem no encontrado")
            }
        }
    }
}
