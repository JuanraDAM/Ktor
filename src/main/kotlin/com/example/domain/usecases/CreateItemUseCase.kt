package com.example.domain.usecases

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository

class CreateItemUseCase(private val itemRepository: ItemRepository) {

    suspend fun invoke(request: com.example.presentation.routes.CreateItemRequest): Item {
        require(request.title.isNotBlank()) { "El título del ítem no puede estar vacío" }
        return itemRepository.createItem(request)
    }
}
