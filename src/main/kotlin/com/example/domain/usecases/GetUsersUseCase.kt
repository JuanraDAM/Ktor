package com.example.domain.usecases

import com.example.domain.models.User
import com.example.domain.repositories.UserRepository

class GetUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): List<User> {
        return repository.getUsers()
    }
}
