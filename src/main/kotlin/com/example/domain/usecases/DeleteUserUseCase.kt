package com.example.domain.usecases

import com.example.domain.repositories.UserRepository

class DeleteUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): Boolean {
        return repository.deleteUser(id)
    }
}
