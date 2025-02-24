package com.example.domain.usecases

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository

class CreateItemUseCase(private val repository: ItemRepository) {
    /**
     * Crea un nuevo ítem (card) y devuelve su ID.
     */
    suspend operator fun invoke(item: Item): Int {
        return repository.createItem(item)
    }
}
