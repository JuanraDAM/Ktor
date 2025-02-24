package com.example.domain.usecases

import com.example.domain.models.User
import com.example.domain.repositories.UserRepository

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int, user: User): Boolean {
        return repository.updateUser(id, user)
    }
}
