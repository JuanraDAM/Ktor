package com.example.domain.usecases

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository
import com.example.presentation.routes.CreateItemRequest
import com.example.utils.saveBase64Image

class CreateItemUseCase(private val itemRepository: ItemRepository) {

    suspend fun invoke(request: CreateItemRequest): Item {
        require(request.title.isNotBlank()) { "El título del ítem no puede estar vacío" }
        // Guardar la imagen en la carpeta del usuario, obteniendo la ruta del fichero
        val imagePath = saveBase64Image(request.image, request.userId)
        // Actualizar el request con la ruta en lugar de la cadena Base64
        val updatedRequest = request.copy(image = imagePath)
        return itemRepository.createItem(updatedRequest)
    }
}
