package com.example.domain.usecases

import com.example.domain.repositories.ItemRepository

class DeleteItemUseCase(private val repository: ItemRepository) {
    /**
     * Elimina un ítem (card) dado su ID.
     * Devuelve true si se eliminó correctamente.
     */
    suspend operator fun invoke(id: Int): Boolean {
        return repository.deleteItem(id)
    }
}
