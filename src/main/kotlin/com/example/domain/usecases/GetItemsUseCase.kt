package com.example.domain.usecases

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository

class GetItemsUseCase(private val repository: ItemRepository) {
    /**
     * Devuelve la lista de todos los ítems (cards).
     */
    suspend operator fun invoke(): List<Item> {
        return repository.getAllItems()
    }
}
