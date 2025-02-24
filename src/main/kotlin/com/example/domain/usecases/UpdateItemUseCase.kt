package com.example.domain.usecases

import com.example.domain.models.Item
import com.example.domain.repositories.ItemRepository

class UpdateItemUseCase(private val repository: ItemRepository) {
    /**
     * Actualiza un ítem (card) dado su ID.
     * Devuelve true si la actualización fue exitosa.
     */
    suspend operator fun invoke(id: Int, item: Item): Boolean {
        return repository.updateItem(id, item)
    }
}
